package xyz.taotao.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import xyz.taotao.common.jedis.JedisClient;
import xyz.taotao.common.pojo.EUDateGridResult;
import xyz.taotao.common.pojo.TaotaoResult;
import xyz.taotao.common.util.IDUtils;
import xyz.taotao.common.util.JsonUtils;
import xyz.taotao.mapper.TbItemDescMapper;
import xyz.taotao.mapper.TbItemMapper;
import xyz.taotao.mapper.TbItemParamItemMapper;
import xyz.taotao.pojo.*;
import xyz.taotao.service.ItemService;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;
    @Autowired
    private TbItemParamItemMapper itemParamItemMapper;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination topicDestination;

    @Autowired
    private JedisClient jedisClient;
    @Value("ITEM_INFO")
    private String REDIS_ITEM_PRE;
    @Value("3600")
    private Integer ITEM_CACHE_EXPIRE;

    @Override
    public TbItem getItemById(long itemId){
        try{
            String json = jedisClient.get(REDIS_ITEM_PRE+":"+itemId+":BASE");
            if(StringUtils.isNotBlank(json)){
                TbItem tbItem = JsonUtils.jsonToPojo(json,TbItem.class);
                return tbItem;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(itemId);
        List<TbItem> list = itemMapper.selectByExample(example);
        if (list!=null&&list.size()>0) {
            try {

                jedisClient.set(REDIS_ITEM_PRE + ":" + itemId + ":BASE", JsonUtils.objectToJson(list.get(0)));
                //设置过期时间
                jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":BASE", ITEM_CACHE_EXPIRE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list.get(0);
        }
        return null;
    }
    @Override
    public EUDateGridResult getItemList(Integer page,Integer rows){
        TbItemExample example = new TbItemExample();

        PageHelper.startPage(page,rows);
        List<TbItem> list = itemMapper.selectByExample(example);
        EUDateGridResult result = new EUDateGridResult();
        result.setRows(list);
        PageInfo pageInfo = new PageInfo<>(list);
        result.setTotal(pageInfo.getTotal());
        return  result;
    }
    @Override
    public TbItemDesc getItemDescById(long itemId){
        try{
            String json = jedisClient.get(REDIS_ITEM_PRE+":"+itemId+":DESC");
            if (StringUtils.isNoneBlank(json)){
                TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(json,TbItemDesc.class);
                return tbItemDesc;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemId);
        try{
            jedisClient.set(REDIS_ITEM_PRE+":"+itemId+":DESC",JsonUtils.objectToJson(itemDesc));
            jedisClient.expire(REDIS_ITEM_PRE + ":" +itemId +":DESC",ITEM_CACHE_EXPIRE);
        }catch (Exception e){
            e.printStackTrace();
        }
        return itemDesc;
    }
    @Override
    public TaotaoResult createItem(TbItem item,String desc,String itemParam) throws Exception{
        final Long itemId = IDUtils.genItemId();
        item.setId(itemId);
        item.setStatus((byte)1);
        item.setCreated(new Date());
        item.setUpdated(new Date());
        itemMapper.insert(item);
        TaotaoResult result = insertItemDesc(itemId,desc);
        if (result.getStatus()!=200){
            throw new Exception();
        }
        //添加规格参数
        result = insertItemParamItem(itemId,itemParam);
        if(result.getStatus()!=200){
            throw  new Exception();
        }
        //发送商品添加信息
        jmsTemplate.send(topicDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(itemId+"");
                return textMessage;
            }
        });
        return TaotaoResult.ok();
    }
    //添加商品描述
    private TaotaoResult insertItemDesc(Long itemId,String desc){
        TbItemDesc itemDesc = new TbItemDesc();
        itemDesc.setItemId(itemId);
        itemDesc.setItemDesc(desc);
        itemDesc.setCreated(new Date());

        itemDescMapper.insert(itemDesc);
        return TaotaoResult.ok();
    }
    //添加参数规格
    private TaotaoResult insertItemParamItem(Long ItemId,String itemParam){
        TbItemParamItem itemParamItem = new TbItemParamItem();
        itemParamItem.setItemId(ItemId);
        itemParamItem.setParamData(itemParam);
        itemParamItem.setCreated(new Date());
        itemParamItem.setUpdated(new Date());
        itemParamItemMapper.insert(itemParamItem);
        return TaotaoResult.ok();

    }

}










