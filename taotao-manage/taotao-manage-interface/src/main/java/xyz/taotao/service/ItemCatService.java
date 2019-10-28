package xyz.taotao.service;

import xyz.taotao.common.pojo.EUTreeNode;

import java.util.List;

public interface ItemCatService{
    List<EUTreeNode> getItemCatList(long parentId);
}