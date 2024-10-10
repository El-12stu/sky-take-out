package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造Redis当中的key
        String key="dish_"+categoryId;
        //查询Redis中是否存在菜品数据
            ValueOperations valueOperations=redisTemplate.opsForValue();
            List<DishVO> list=(List<DishVO>) valueOperations.get(key);
        //如果存在无需调用数据库
        if(list != null && list.size()>0){

            return Result.success(list);
        }
        //若不存在缓存中，查询数据库，则储存至redis中
        else{

            Dish dish=new Dish();
            dish.setCategoryId(categoryId);
            dish.setStatus(StatusConstant.ENABLE);
            //查询数据库
            list=dishService.listWithFlavor(dish);
            //储存至redis中
            valueOperations.set(key,list);

            return Result.success(list);
        }



    }

}
