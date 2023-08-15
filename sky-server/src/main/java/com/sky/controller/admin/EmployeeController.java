package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api( tags = "登录信息")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation(value = "登录方法")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @ApiOperation("新增操作")
    @PostMapping
    public Result save ( @RequestBody EmployeeDTO employeeDTO)
    {
        log.info( "新增员工 {} " , employeeDTO); // log 的占位符
        employeeService.save( employeeDTO);

        return Result.success();
    }
    @ApiOperation("员工 分页查询")
    @GetMapping(value="/page")
    // 封装的是 page result 的类似数据
    // 因为这里是query 的 请求形式, ? key=value
    // 直接通过对应 相同属性名的 dto 直接接受参数即可
    // 因为不是请求体传来的, 所以不用requestBody
    public Result<PageResult> page( EmployeePageQueryDTO  employeePageQueryDTO)
    {
        log.info("员工分类查询,  参数为 {}" , employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO) ;

        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工
     * @param status
     * @param id
     * @return
     */
    // 这里不需要泛型, 因为不需要返回前端数据, 不是查询操作
    // 只需要返回一个code 即可 , 根据 需求进行分析即可
    @ApiOperation("启用和禁用员工")
    @PostMapping("/status/{status}")
    // 路径参数需要通过pathvariable去修饰 , 而id 非路径参数, 是 Query形式的, 直接 写到形参中就可以自动匹配 ,保证名字一致即可
    // 注意路径参数和query参数的区别! variable和param 注解的value 是在名字不一致的 时候使用的 ,用于匹配
    public  Result startOrStop(@PathVariable("status") Integer status ,  Long id)
    {
        log.info("启用禁用员工账号状态{} , id {}" , status, id );
        employeeService.startOrStop(status , id); // 需要我们的service 返回一个什么数据?
        //
        return Result.success();
    }
}
