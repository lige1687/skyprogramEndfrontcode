package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /***
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee() ; // 一般来说, 返回给 controller 的数据得是一个 entity实体类

        BeanUtils.copyProperties( employeeDTO , employee) ; // 通过springBean 的utils类 进行 两个有相同属性字段的类型转换
// 前提是属性名得一致 ,其余的多出来的数据需要自己去设置
        // 这里status 默认1 是正常, 如果直接写 1 就是硬编码了 , 不便于维护
        // 此时通过一个StatusConstant 枚举类 ,去规范的书写 表示状态 , 优雅!

        employee.setStatus(StatusConstant.ENABLE);
        // 注意密码使用默认的 123456, 不过由于我们的加密md5, 需要通过工具类 转换为 暗文 后set
        // Digest Utils 也是spring提供的 , 和Beanutils一样
        // 当然默认密码也是 通过常量引入, 解决硬编码问题
        // 便于维护
         employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
      // 设置修改时间
       employee.setCreateTime(LocalDateTime.now());
       employee.setUpdateTime(LocalDateTime.now());
       //设置创建用户id
        // # TODO 目前无法解决, 等后续学到一个技术,才能获取到对应的 创建用户的 值
        // 这里先写死
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        // 最后调用持久层进行  数据的插入
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 基于mybatis 的pagehelper 实现 分页查询
                // 基于mybatis 的拦截器, 进行sql语句的加强 ,动态的拼接 一段limit语句用于分页查询
        // 或者mybatisplus 也可以实现
        PageHelper.startPage(employeePageQueryDTO.getPage() , employeePageQueryDTO.getPageSize()); // 在哪一页开始? 查询多少条?
        // 接下来调用的sql语句就会被自动增强, 添加limit的分页语句
        Page<Employee> page =employeeMapper.pageQuery(employeePageQueryDTO) ; // 注意遵循helper 的 使用规则, 返回Page 对象
        // 泛型使用employee实体, 因为这里是查询数据库, 必须一一对应

        // 注意page其实封装的是一个泛型的 集合, list , 是一个数组
        // 最后要返回一个pageResult对象, 所以需要对 page 进行解析
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
             // 启用和禁用本质是一个sql , 修改操作, 修改员工的 status 字段 ,根据id修改
        // update employee set status=? where id=?
        // 我们希望的是书写动态sql , 这样不论传入的是id  还是什么用户名, 都可以去修改 对应的status
        //提升复用性!  所以这里直接给mapper传入  参数不合理, 写死了
        Employee employee = Employee.builder().status(status).id(id).build();
        // 通过lombo提供的@Builder 注解, 对类的链式 set
        // 以 类名.builder (). 属性名(值).属性名(值).build () 表示构造结束



        employeeMapper.update(employee);

    }
}
