package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    // 因为涉及 菜品的查询 操作
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void save(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        // 由于对象的某些字段一致, 直接使用
        // 为什么不使用 dto 的对象进行查询? 因为查询的是实体类, 最好还是使用实体类进行接受, 属性更加齐全!
        // 因为是select * 要求所有的信息, dto只能传递部分的信息 ,
        BeanUtils.copyProperties(shoppingCartDTO ,shoppingCart);
        // 但是不够, 查询购物车还需要userid , 所以这里必须 手动的获取userid( 因为dto里面没有userid ,但是 mapper查询需要使用
        // 想到了 threadLocal , 在 拦截器 jwt解析的时候就能获取到 userid, 并且存在 了 BaseContext中 ( jwt拦截器的内容
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);


        // 如果要添加的商品已经在购物车存在, 直接将对应的number +1 即可
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 根据我们的xml的编写,此时只可能获取到一条数据, 因为 我们对判断的条件进行 了细分, 一个菜品就算 风味不同也是 不同的购物车

if ( list !=null && list.size()>0 )
{// 所以只能查询到一个数据 , 所以list 直接 获得 0位置的即可
    ShoppingCart cart = list.get(0) ;
    cart.setNumber(cart.getNumber()+1);
shoppingCartMapper.updateNumberById(cart); // 更新数量
}
else {  // 不存在, 就重新插入一个 购物车的数据
    // 我们需要构建商品的名称 , 价格,  前端没给提交 ,所以任然需要查询
    // 如果是菜品, 就去菜品表去查, 如果是套餐, 就去套餐表查
    //   一条数据代表了一个 用户的购物车

    Long dishId = shoppingCartDTO.getDishId();
    if (dishId  != null)
    {
        // 添加的是一个菜品
        Dish dish = dishMapper.getByiId(dishId);
    shoppingCart.setName(dish.getName());
shoppingCart.setImage(dish.getImage());
// TODO 但是这里的 amount 应该是总价啊, 设计有问题吧
shoppingCart.setAmount(dish.getPrice()); //设置金额, 注意 字段不一致

        // 检查还要设置的属性  ( 属性拷贝已经完成一部分了


    }
    else  // 添加的是 套餐
    {
        Long setmealId = shoppingCartDTO.getSetmealId();
        Setmeal setmeal = setmealMapper.getById(setmealId);
        // 查出套餐对象后, 进行冗余字段的赋值
        shoppingCart.setName(setmeal.getName());
        shoppingCart.setImage(setmeal.getImage());
        shoppingCart.setAmount(setmeal.getPrice());



    }
    // 都需要的代码最后写 一起即可
    shoppingCart.setNumber(1);
    shoppingCart.setCreateTime(LocalDateTime.now());
    // 统一的插入 shoppingCart购物车数据 到 cart表中
    shoppingCartMapper.insert(shoppingCart) ;

}

        // 根据 userid 去判断这是哪个用户的购物车, 也就是一个update操作 !

        // 如果不存在, 才是一个insert 的操作, 插入一个购物车

    }

    /**
     *查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long currentId = BaseContext.getCurrentId();
        // 通过ThreaLocal 获取 userid即可
        ShoppingCart cart = new ShoppingCart();
        cart.setId(currentId);
        // 利用已经创建好的查询 mapper方法, 但是人家需要一个 cart对象传入 ,以便 动态sql的 书写
        // 所以这里手动构建一个即可

        List<ShoppingCart> list = shoppingCartMapper.list(cart);// 当前只需要传userid即可 查询
return  list ;
    }

    @Override
    public void cleanShoppingCart() {
        // 根据userid进行删除即可
        Long currentId = BaseContext.getCurrentId();

        shoppingCartMapper.deleteByUserId (currentId);

    }
}
