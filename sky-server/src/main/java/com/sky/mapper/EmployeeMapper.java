package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /***
     * 插入员工数据
     * @param employee
     */
    @Insert("insert into employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)  values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber},#{status} , #{createTime} ,#{updateTime}, #{createUser} , #{updateUser})"

            )
   // 注意 这里通过 #{ }  进行解析 employee 参数中的属性, 使用的是驼峰命名, myabtis 中设置了 表下划线和驼峰命名 的转换
    // 所以可以识别 属性, 注意顺序要对

    void insert(Employee employee);


    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void update(Employee employee);

    // 较为简单,直接通过 注解查询
    @Select("select * from employee where id='#{id}'  ")
    Employee getById(Long id);
}
