package com.libraryinformation.exception;


import com.libraryinformation.stateCode.Code;
import com.libraryinformation.domain.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理类
 *
 * @author ZhouTX
 */
//@RestControllerAdvice
public class ProjectExceptionAdvice {
    /**
     * 系统异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException ex) {
        //记录日志
        //发送消息给运维
        //发送邮件给开发人员,ex对象发送给开发人员
        return new Result(ex.getCode(), null, ex.getMessage());
    }

    /**
     * 业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException ex) {
        return new Result(ex.getCode(), null, ex.getMessage());
    }

    /**
     * 其他未知异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result doOtherException(Exception ex) {
        //记录日志
        //发送消息给运维
        //发送邮件给开发人员,ex对象发送给开发人员
        return new Result(Code.SYSTEM_UNKNOW_ERR, null, "系统繁忙，请稍后再试！");
    }
}
