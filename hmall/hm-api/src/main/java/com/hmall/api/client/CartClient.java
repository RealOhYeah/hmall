package com.hmall.api.client;


import com.hmall.api.domain.vo.CartVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@FeignClient("cart-service")
public interface CartClient {


    //批量删除购物车中商品
    @DeleteMapping("/carts")
    void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);

    //查询购物车列表
    @GetMapping
    public List<CartVO> queryMyCarts();




}
