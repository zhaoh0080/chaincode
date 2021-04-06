# chaincode
公益家
* 一、Fabric 安装与使用
Fabric 安装教程可参考博客：
https://blog.csdn.net/qq_28540443/article/details/104265844
腾讯云主机上的 Fabric 版本为 2.1，安装目录为：
 /block/gopath/src/github.com/hyperledger/fabric

* 二、智能合约 Chaincode 开发
1、常用 API 介绍
Java 版本 Chaincode 的一些 API 介绍，参考阿里云文档：
https://help.aliyun.com/document_detail/141372.html?spm=a2c4g.11186623.6.606.281c379evInTeZ
2、Chaincode 详解
1）功能介绍
编写智能合约来存储下图中数据表里的数据。

在 Java Chaincode 中，实现：
用户操作记录的创建。
操作记录的存储、查询、更新、删除。
 Hyperledger Fabric 中有两个数据库选项用于保存超级账本的 world state：LevelDB 和 CouchDB，在这里，为了简单起见，使用 LevelDB。
2）具体实现
Fabric Java 版本的 Chaincode 默认使用的是 Gradle 进行构建（也可以使用 Maven），所以这里也创建 Gradle 项目。在 Idea 中创建 Gradle 项目。打开 settings.gradle，添加：
rootProject.name = 'fabric-chaincode-gradle'

在 build.gradle 中添加以下内容：
/*
 * Copyright IBM Corp. 2018 All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id 'com.github.johnrengelman.shadow' version '5.1.0'
    id 'java'
}

group 'org.hyperledger.fabric-chaincode-java'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url "https://hyperledger.jfrog.io/hyperledger/fabric-maven"
    }
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.+'
    implementation 'com.owlike:genson:1.5'
    compile 'com.google.code.gson:gson:2.8.6'
}

shadowJar {
    baseName = 'chaincode'
    version = null
    classifier = null

    manifest {
        attributes 'Main-Class': 'org.hyperledger.fabric.samples.record.RecordChaincode'
    }
}

创建用户操作记录类 Record：
/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.record.Models;

import java.math.BigDecimal;
import java.util.Date;

public final class Record {

    private Integer id;

    private String title;

    private Long userId ;

    private Integer linkId;

    private String category;

    private String integraltype;

    private BigDecimal amount;

    private BigDecimal balance;

    private Date createTime;

    private Date modifyTime;

    private Integer settlementRate;

    private Integer status;

    private String mark;

    private Integer pm;

    private Integer succsign;
    
    public Record() {
    }

    public Record(Integer id, String title, Long userId, Integer linkId, String category, String integraltype, BigDecimal amount, BigDecimal balance, Date createTime,Date modifyTime, Integer settlementRate, Integer status, String mark, Integer pm, Integer succsign) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.linkId = linkId;
        this.category = category;
        this.integraltype = integraltype;
        this.amount = amount;
        this.balance = balance;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.settlementRate = settlementRate;
        this.status = status;
        this.mark = mark;
        this.pm = pm;
        this.succsign = succsign;
    }
}

Chaincode 编写：
package org.hyperledger.fabric.samples.record;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.samples.record.Models.Record;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RecordChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(RecordChaincode.class);

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    //main
    public static void main(String[] args) {
        new RecordChaincode().start(args);
    }

    /**
     * @param stub
     * @return init chaincode
     */
    @Override
    public Response init(ChaincodeStub stub) {

        return newSuccessResponse("Init");
    }

    /**
     * @param stub
     * @return invoke chaincode
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke java chaincode");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("createRecord")) {
                return createRecord(stub, params);
            }
            if (func.equals("getRecord")) {
                return getRecord(stub, params);
            }
            if (func.equals("getRecordByUser")) {
                return getRecordByUser(stub, params);
            }
            if (func.equals("updateRecord")) {
                return updateRecord(stub, params);
            }
            if (func.equals("deleteRecord")) {
                return deleteRecord(stub, params);
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"createRecord\", \"getRecord\",\"getRecordByUser\", \"updateRecord\", \"deleteRecord\"]");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    /**
     * @param stub
     * @param args
     * @return 保存一条记录
     */
    private Response createRecord(ChaincodeStub stub, List<String> args) {
        if (args.size() > 30 || (args.size() % 2) != 0 || args.size() < 2)
            return newErrorResponse("Incorrect number of arguments");
        Record record = new Record();
        String gsonRecord = gson.toJson(record);
        JSONObject jsonObject = new JSONObject(gsonRecord);
        String recordId = args.get(1);
        try {
            Integer id = Integer.parseInt(recordId); //检验参数是否为整数id
            String recordState = stub.getStringState(recordId);
            if (!recordState.isEmpty()) {
                String errorMessage = String.format("Record %s already exists", recordId);
                return newErrorResponse(errorMessage);
            }

            for (int i = 0; i < args.size(); i++) {
                jsonObject.put(args.get(i), args.get(++i));
            }
            record = gson.fromJson(jsonObject.toString(), Record.class);
            recordState = gson.toJson(record);
            stub.putStringState(recordId, recordState);
        } catch (Exception e) {
            return newErrorResponse("Incorrect types of arguments");
        }
        String response = "Create record successfully";
        return newSuccessResponse(response, ByteString.copyFrom(response, UTF_8).toByteArray());
    }

    /**
     * @param stub
     * @param args
     * @return 以id查询一条记录
     */
    private Response getRecord(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse("Incorrect number of arguments, expecting 1");

        String recordId = args.get(0);
        String recordState = stub.getStringState(recordId);

        if (recordState.isEmpty() || recordState == null) {
            String errorMessage = String.format("Record %s does not exist", recordId);
            return newErrorResponse(errorMessage);
        }
        Record record = gson.fromJson(recordState, Record.class);
        String result = gson.toJson(record);

        return newSuccessResponse(result, ByteString.copyFrom(result, UTF_8).toByteArray());
    }

    /**
     * @param stub
     * @param args
     * @return 修改一条记录
     */
    private Response updateRecord(ChaincodeStub stub, List<String> args) {
        if (args.size() > 30 || (args.size() % 2) != 0 || args.size() < 2)
            return newErrorResponse("Incorrect number of arguments");

        String recordId = args.get(1);

        String recordState = "";
        JSONObject jsonObject = null;

        try {
            recordState = stub.getStringState(recordId);
            if (recordState.isEmpty()) {
                String errorMessage = String.format("Record %s does not exist", recordId);
                return newErrorResponse(errorMessage);
            }
            jsonObject = new JSONObject(recordState);
            for (int i = 0; i < args.size(); i++) {
                jsonObject.put(args.get(i), args.get(++i));
            }
            Integer id = Integer.parseInt(recordId);
            Record record = gson.fromJson(jsonObject.toString(),Record.class);
            stub.putStringState(recordId, gson.toJson(record));
        } catch (Exception e) {
            return newErrorResponse("Incorrect types of arguments");
        }

        String response = "Update record successfully";
        return newSuccessResponse(response, ByteString.copyFrom(response, UTF_8).toByteArray());
    }

    /**
     * @param stub
     * @param args
     * @return 删除一条记录
     */
    private Response deleteRecord(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse("Incorrect number of arguments, expecting the id of the record");
        String recordId = args.get(0);
        String recordState = stub.getStringState(recordId);
        try {
            Integer id = Integer.parseInt(recordId);
        } catch (Exception e) {
            return newErrorResponse("Incorrect types of arguments");
        }
        if (recordState.isEmpty() || recordState == null) {
            return newErrorResponse(String.format("Record %s does not exist", recordId));
        }
        stub.delState(recordId);
        String response = "Delete record successfully";
        return newSuccessResponse(response, ByteString.copyFrom(response, UTF_8).toByteArray());
    }

    /**
     * @param stub
     * @param args userId
     * @return 以userId获取该用户的所有记录
     * 需要启动couchDB
     */
    private Response getRecordByUser(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse("Incorrect number of arguments, expecting 1");
        Long userId = null;
        try {
            userId = Long.parseLong(args.get(0));
        } catch (Exception e) {
            return newErrorResponse("Incorrect types of arguments");
        }

        String queryString = String.format("{\"selector\":{\"userId\":%d}}", userId);
        QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);

        String resultById = "";
        if (!results.iterator().hasNext())
            return newErrorResponse(String.format("User %d does not exist", userId));
        boolean sign = false;
        for (KeyValue result : results) {
            if (sign)
                resultById += "\n";
            Record record = gson.fromJson(result.getStringValue(), Record.class);

            resultById += gson.toJson(record);

            sign = true;
        }

        return newSuccessResponse(resultById, ByteString.copyFrom(resultById, UTF_8).toByteArray());

    }
}

Chaincode 中，数据以 key-value 的形式存储在数据库中，这里以 id 作为 key，包含 id 的所有信息作为 value。
项目结构：


三、部署智能合约
1、手动部署
网络使用 Fabric 自带的 First network，其具体路径为 ：
/block/gopath/src/github.com/hyperledger/fabric/scripts/fabric-samples/first-network
在 docker-compose-cli.yaml 文件中，Chaincode 的目录在其上级目录的 /chaincode 文件夹中，所以我们可以将自己的 Chaincode 放在这里，也可以修改此 yaml 文件，自己设置路径。本文档中将编写的 Chaincode 放在这个 /chaincode 目录中。

然后逐步启动网络，具体参考官方文档：
https://hyperledger-fabric.readthedocs.io/en/release-2.0/build_network.html
2、脚本部署（推荐）
这里的脚本指的是我们在测试 First network 时，使用的 byfn.sh，该脚本会自动帮我们部署网络，安装、打包、实例化链码，非常方便。但该脚本文件会默认执行 go 版本的链码，指定 Java 版本链码时需要添加参数：-l java。
设置好 Java 语言后，脚本会默认安装 /chaincode/abstore/java 目录下的链码，所以需要将我们项目中的这三个文件夹放到这个 /java 目录中，替换原来的文件（可以将原来的文件做个备份，如将 /java 目录换个名字）：

此外，脚本会帮我们测试链码的一些功能（这些功能是 abstore 中的简单资产转移），我们不需要进行这些测试，所以修改 /first-network/scripts 下的 script.sh 脚本文件，将最后几行的 query 和 invoke 命令注释掉：

修改完后，进入 First network 目录，运行 ./byfn up -l java 命令启动网络，部署 Chaincode。部署完成后，我们进入 cli 容器，进行几个测试。
docker exec -it cli bash

创建记录：
peer chaincode invoke -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"Args":["createRecord","id","1","title","首签赠送","userId","25","linkId","0","category","integral","integraltype","sign","amount","1.00","balance","100.00","createTime","2020-05-09 00:00:00","modifyTime","2020-05-09 00:00:00","settlementRate","1","status","1","mark","来了","pm","2","succsign","2"]}'
peer chaincode invoke -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"Args":["createRecord","id","2","title","首签赠送","userId","25","succsign","2","category","integral","integraltype","sign","amount","1.00"]}'

以上两个命令创建了两条记录。
查询：
peer chaincode query -C mychannel -n mycc -c '{"Args":["getRecord","1"]}'

删除：
peer chaincode invoke -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"Args":["deleteRecord","1"]}'

更新：
peer chaincode invoke -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"Args":["updateRecord","id","1","balance","200"]}'

上述命令将 id 为 1 的记录的 balance 修改为 200。
另外，Chaincode 还提供了高级查询功能，比如查询用户 id 为 25 的所有记录，但需要启动 couchDB，启动命令为：./byfn up -l java -s couchdb，有兴趣的话可以试试 couchDB，查询命令为：
peer chaincode query -C mychannel -n mycc -c '{"Args":["getRecordByUser","25"]}'


四、fabricSDKWEB 介绍
这是一个用 Spring Boot 启动的项目，提供 http 访问接口，其中采用 fabric-gateway 与区块链网络交互。
1、项目结构介绍
项目结构：

FabricController：API 访问入口，所有请求路径都在 FabricController。
sdk2.Config：配置文件。
sdk2.FabricSDKService: Fabric SDK 主要方法在此目录。
resources 文件件下存储资源文件。
crypto-config: 服务器上拷贝过来的文件夹（本机测试时需要拷贝此文件夹，云主机上部署时使用了绝对路径，因此不需要拷贝它）。
application.yml : 服务配置文件，里面可以设定启动端口号。
connection.json : 不需要修改。
logback.xml : 日志配置文件。
FabricController：Controller入口文件，在代码中有注释。
FabricSDKService: 主要 service 方法，代码中有注释。
2、本机测试
需要使用软件 postman，测试前需要对代码进行两处简单修改：

上图为服务器部署时的设置，若要本地测试，改为：

同时需要将 /block/gopath/src/github.com/hyperledger/fabric/scripts/fabric-samples/first-network/crypto-config
目录拷贝到项目中的 resources 中。
启动 postman，在 postman 中可执行一些对区块链数据库的操作，如查询 id 为 1 的记录的信息。

无返回结果，而控制台输出：Record 1 does not exist。表明该记录并没有存储。


当 id 换为 206 时，可以查询到其信息。


五、fabricSDKWEB 的部署方式
1) 在本地工程目录打包：mvn clean package -Dmaven.test.skip=true
其中 -Dmaven.test.skip=true 是为了排除项目中的 test 包。
2) 将打好的包（在 target 目录下）传到云服务器 /block 目录下，运行 nohup java -jar fabricWEBSDK-0.0.1-SNAPSHOT.jar &  命令。nohup 是后台运行，执行完以后按回车可以回到命令行继续操作，原本在控制台输出的日志会在 nohup.out 中。以下命令查看日志：
tail -n 100 nohup.out
或 tail -f nohup.out

注意，重新部署区块链网络的时候，需要先 kill 掉 fabricSDKWEB 进程：
ps -ef | grep java  #查询进程号
kill -s 9 进程号

部署完后也要重新部署 fabricSDKWEB
