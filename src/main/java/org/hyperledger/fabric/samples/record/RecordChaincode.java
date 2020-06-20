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
