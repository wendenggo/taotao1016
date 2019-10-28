package xyz.taotao.service;

import xyz.taotao.common.pojo.EUDateGridResult;
import xyz.taotao.common.pojo.TaotaoResult;
import xyz.taotao.pojo.TbItem;
import xyz.taotao.pojo.TbItemDesc;

public interface ItemService{
    EUDateGridResult getItemList(Integer page,Integer rows);
    TaotaoResult createItem(TbItem item,String desc,String itemParam) throws Exception;
    TbItem getItemById(long itemId);
    TbItemDesc getItemDescById(long itemId);



















}