package xyz.taotao.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import xyz.taotao.common.jedis.JedisClient;
import xyz.taotao.common.pojo.EUDateGridResult;
import xyz.taotao.common.util.JsonUtils;
import xyz.taotao.mapper.TbItemDescMapper;
import xyz.taotao.mapper.TbItemMapper;
import xyz.taotao.mapper.TbItemParamItemMapper;
import xyz.taotao.pojo.TbItem;
import xyz.taotao.pojo.TbItemExample;
import xyz.taotao.service.ItemService;

import javax.print.attribute.standard.Destination;
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
    @Autowired
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
    public EUDateGridResult getItem


}










