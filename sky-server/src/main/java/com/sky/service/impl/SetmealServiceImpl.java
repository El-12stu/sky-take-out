package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //新增setmeal表
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId=setmeal.getId();
        //新增setmeal_dish表
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {setmealDish.setSetmealId(setmealId);});
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //根据套餐ID查询套餐表信息
        Setmeal setmeal=setmealMapper.getById(id);
        //根据套餐ID查询套餐-菜品关联表信息
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
        //封装数据为SetmealVO
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }




    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        //判断是否存在起售中的套餐
        for (Long id:ids){
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除setmeal表的信息
        setmealMapper.deleteByIds(ids);
        //删除setmeal_dish表的信息
        setmealDishMapper.deleteBySetmealIds(ids);


    }
    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        //需要在查询功能之前开启分页功能：当前页的页码   每页显示的条数
        PageHelper.startPage(pageNum, pageSize);
        //这个方法有返回值为Page对象，里面保存的是分页之后的相关数据
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        //封装到PageResult中:总记录数  当前页数据集合
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 套餐起售、停售
     * @param setmealId
     * @param status
     */
    public void startOrStop(Long setmealId, Integer status){
        Setmeal setmeal=new Setmeal();
        setmeal.setId(setmealId);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }


    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //修改套餐表
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        Long setmealId=setmealDTO.getId();
        //修改套餐-菜品表
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        //思路：先删除，后添加
        setmealDishMapper.deleteBySetmealId(setmealId);
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }
}
