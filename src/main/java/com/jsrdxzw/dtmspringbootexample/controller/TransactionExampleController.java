package com.jsrdxzw.dtmspringbootexample.controller;

import com.jsrdxzw.dtmspringbootexample.controller.model.ResultData;
import com.jsrdxzw.dtmspringbootexample.controller.model.TransReq;
import com.jsrdxzw.dtmspringbootexample.dao.mapper.UserAccountMapper;
import com.jsrdxzw.dtmspringbootstarter.annotations.DtmBarrier;
import com.jsrdxzw.dtmspringbootstarter.annotations.DtmResponse;
import com.jsrdxzw.dtmspringbootstarter.core.barrier.BranchBarrier;
import com.jsrdxzw.dtmspringbootstarter.core.http.HttpClient;
import com.jsrdxzw.dtmspringbootstarter.core.http.vo.DtmServerResult;
import com.jsrdxzw.dtmspringbootstarter.core.http.vo.TransactionResponse;
import com.jsrdxzw.dtmspringbootstarter.core.msg.Msg;
import com.jsrdxzw.dtmspringbootstarter.core.saga.Saga;
import com.jsrdxzw.dtmspringbootstarter.core.tcc.Tcc;
import com.jsrdxzw.dtmspringbootstarter.exception.DtmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author xuzhiwei
 * @date 2022/4/11 17:38
 */
@RestController
public class TransactionExampleController {
    public static final int TRANSOUT_UID = 1;

    private static final int TRANSIN_UID = 2;

    private static final String HOST = "http://localhost:9090";

    @Autowired
    private HttpClient httpClient;

    @Value("${dtm.http-server}")
    private String httpServer;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @DtmResponse
    @PostMapping("/SagaBTransOut")
    public ResultData sagaTransOut(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("SagaBTransOut");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaBTransOut call");
            userAccountMapper.sagaAdjustBalance(TRANSOUT_UID, transReq.getAmount().negate());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/SagaBTransIn")
    public ResultData sagaTransIn(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("SagaBTransIn");
        System.out.println(branchBarrier);
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaBTransIn call");
            userAccountMapper.sagaAdjustBalance(TRANSIN_UID, transReq.getAmount());
//            throw DtmException.failure();
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/SagaBTransOutCom")
    public ResultData sagaTransOutCom(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("SagaBTransOutCom");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaBTransOutCom call");
            userAccountMapper.sagaAdjustBalance(TRANSOUT_UID, transReq.getAmount());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/SagaBTransInCom")
    public ResultData sagaTransInCom(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("SagaBTransInCom");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaBTransInCom call");
            userAccountMapper.sagaAdjustBalance(2, transReq.getAmount().negate());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/SagaB2TransIn")
    public ResultData sagaB2TransIn(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("SagaB2TransIn");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaB2TransIn call");
            userAccountMapper.sagaAdjustBalance(2, transReq.getAmount().divide(BigDecimal.valueOf(2)));
        });

        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("SagaB2TransIn call");
            userAccountMapper.sagaAdjustBalance(2, transReq.getAmount().divide(BigDecimal.valueOf(2)));
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/SagaB2TransInCom")
    public ResultData sagaB2TransInCom(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        branchBarrier.call(transactionManager, barrier -> {
            userAccountMapper.sagaAdjustBalance(2, transReq.getAmount().divide(BigDecimal.valueOf(2)).negate());
        });

        branchBarrier.call(transactionManager, barrier -> {
            userAccountMapper.sagaAdjustBalance(2, transReq.getAmount().divide(BigDecimal.valueOf(2)).negate());
        });
        return ResultData.success();
    }

    @PostMapping("/http_saga_barrier_twice")
    public String httpSagaBarrierTwice() {
        TransReq req = new TransReq();
        req.setAmount(BigDecimal.valueOf(100));
        new Saga(httpClient)
                .add(HOST + "/SagaBTransOut", HOST + "/SagaBTransOutCom", req)
                .add(HOST + "/SagaB2TransIn", HOST + "/SagaB2TransInCom", req)
                .submit();
        return "ok";
    }

    @PostMapping("/http_saga_wait")
    public String httpSagaWait() {
        TransReq req = new TransReq();
        req.setAmount(BigDecimal.valueOf(30));
        DtmServerResult result = new Saga(httpClient)
                .add(HOST + "/SagaBTransOut", HOST + "/SagaBTransOutCom", req)
                .add(HOST + "/SagaB2TransIn", HOST + "/SagaB2TransInCom", req)
                .waitResult()
                .submit();
        System.out.println("wait result ok " + result);
        return "ok";
    }

    @PostMapping("/http_concurrent_saga")
    public String httpConcurrentSaga() {
        TransReq req = new TransReq();
        req.setAmount(BigDecimal.valueOf(30));
        DtmServerResult result = new Saga(httpClient)
                .add(HOST + "/SagaBTransOut", HOST + "/SagaBTransOutCom", req)
                .add(HOST + "/SagaBTransOut", HOST + "/SagaBTransOutCom", req)
                .add(HOST + "/SagaBTransIn", HOST + "/SagaBTransInCom", req)
                .add(HOST + "/SagaBTransIn", HOST + "/SagaBTransInCom", req)
                .enableConcurrent()
                .addBranchOrder(2, Arrays.asList(0, 1))
                .addBranchOrder(3, Arrays.asList(0, 1))
                .submit();
        return "ok";
    }

    @PostMapping("/sagaSubmit")
    public TransactionResponse sagaSubmit() {
        TransReq req = new TransReq();
        req.setAmount(BigDecimal.valueOf(100));
        DtmServerResult submit = new Saga(httpClient)
                .add(HOST + "/SagaBTransOut", HOST + "/SagaBTransOutCom", req)
                .add(HOST + "/SagaBTransIn", HOST + "/SagaBTransInCom", req)
                .submit();
        return TransactionResponse.dtmSuccess();
    }


    @DtmResponse
    @PostMapping("/tccTransInTry")
    public ResultData tccTransInTry(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("tccTransInTry");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransInTry call");
            userAccountMapper.tccAdjustTrading(TRANSIN_UID, transReq.getAmount());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/tccTransInConfirm")
    public ResultData tccTransInConfirm(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("tccTransInConfirm");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransInConfirm call");
            userAccountMapper.tccAdjustBalance(TRANSIN_UID, transReq.getAmount());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/tccTransInCancel")
    public ResultData tccTransInCancel(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("tccTransInCancel");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransInCancel call");
            userAccountMapper.tccAdjustTrading(TRANSIN_UID, transReq.getAmount().negate());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/tccTransOutTry")
    public ResultData tccTransOutTry(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println(branchBarrier);
        System.out.println("tccTransOutTry");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransOutTry call");
            userAccountMapper.tccAdjustTrading(TRANSOUT_UID, transReq.getAmount().negate());
//            throw DtmException.failure();
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/tccTransOutConfirm")
    public ResultData tccTransOutConfirm(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("tccTransOutConfirm");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransOutConfirm call");
            userAccountMapper.tccAdjustBalance(TRANSOUT_UID, transReq.getAmount().negate());
        });
        return ResultData.success();
    }

    @DtmResponse
    @PostMapping("/tccTransOutCancel")
    public ResultData tccTransOutCancel(@DtmBarrier BranchBarrier branchBarrier, @RequestBody TransReq transReq) {
        System.out.println("tccTransOutCancel");
        branchBarrier.call(transactionManager, barrier -> {
            System.out.println("tccTransOutCancel call");
            userAccountMapper.tccAdjustTrading(TRANSOUT_UID, transReq.getAmount().negate());
        });
        return ResultData.success();
    }

    @PostMapping("/http_tcc_barrier")
    public String tccBarrierSubmit() {
        TransReq transReq = TransReq.builder().amount(BigDecimal.valueOf(30)).build();
        new Tcc(httpClient).tccGlobalTransaction(tcc -> {
            tcc.callBranch(
                    transReq, HOST + "/tccTransOutTry", HOST + "/tccTransOutConfirm", HOST + "/tccTransOutCancel");
            tcc.callBranch(
                    transReq, HOST + "/tccTransInTry", HOST + "/tccTransInConfirm", HOST + "/tccTransInCancel");
        });
        return "ok";
    }

    @DtmResponse
    @GetMapping("/query")
    public ResultData query() {
        System.out.println("query");
        return ResultData.success();
    }

    @PostMapping("/http_msg")
    public String httpMsg(@RequestParam("page") Integer page) {
        TransReq req = TransReq.builder().amount(BigDecimal.valueOf(30)).build();
        Msg msg = new Msg(httpClient).add(HOST + "/SagaBTransOut", req)
                .add(HOST + "/SagaBTransIn", req);
        msg.prepare(HOST + "/query");
        msg.submit();
        return "ok";
    }

    @PostMapping("/http_msg_doAndCommit")
    public String httpMsgDoAndCommit() {
        TransReq req = TransReq.builder().amount(BigDecimal.valueOf(200)).build();
        Msg msg = new Msg(httpClient).add(HOST + "/SagaBTransIn", req);
        msg.doAndSubmitDb(transactionManager, HOST + "/query", barrier -> {
            System.out.println("submit db");
        });
//        msg.prepare(httpClient, HOST + "/query");
//        msg.submit(httpClient);
        return "ok";
    }
}
