package org.seckill.web;


import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller //@Service @Component
@RequestMapping("/seckill")  // url:/模块/资源/{id}/细分  /seckill/list  类注解是一级URL，方法是二级
public class SeckillController {

    //加入日志 slf4j
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //自动注入
    @Autowired
    private SeckillService seckillService;

    //方法参数可以写ModelAndView 也可以写Model
    //展示列表页
    @RequestMapping(value="/list", method= RequestMethod.GET)
    public String list(Model model){
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);
        //list.jsp + model = ModelAndView

        return "list"; // /WEB-INF/jsp/"list".jsp

    }

    //展示详情页 友好的url
    //控制层职责，接收参数，判断并跳转
    @RequestMapping(value ="/{seckillId}/detail", method=RequestMethod.GET)
    public String detail(Model model, @PathVariable("seckillId") Long seckillId){

        //首先要对传过来的id做判断
        if(seckillId == null){
            //重定向到列表页
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if(seckill == null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        //spring会在WEB-INF文件夹下找detail.jsp
        return "detail";
    }

    //ajax请求  json 所有的ajax请求返回都是SeckillResult
    //用来输出秒杀地址的请求
    @ResponseBody //告诉springMVC返回json类型（ajax输出的默认类型）
    @RequestMapping(value="/{seckillId}/exposer",
            method=RequestMethod.POST,
            produces = {"application/json;charset=utf-8"}) //contentType设置
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId){

        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            //出异常，则success为false
            logger.error(e.getMessage() ,e );
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }

        return result;
    }


    //执行秒杀的方法
    @RequestMapping(value="/{seckillId}/{md5}/execution",
                    method= RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value="killPhone" ,required=false) Long phone){

        //也可以用springMVC valid的功能做一些复杂校验
        if(phone == null){
            //电话号码为空表示没有注册 cookie中没有信息
            return new SeckillResult<SeckillExecution>(false, "未注册");
        }
        SeckillResult<SeckillExecution> result;

        try{

            //SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            //调用存储过程 的业务方法 即优化后
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);


            //seckillResult里的success代表请求是否成功，这些业务异常都算成功
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(RepeatKillException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(SeckillCloseException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(false, execution);
        }

    }

    //获取系统时间的方法
    @RequestMapping(value="/time/now", method=RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }

}
