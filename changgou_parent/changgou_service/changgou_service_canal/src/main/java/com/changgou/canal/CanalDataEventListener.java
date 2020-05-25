package com.changgou.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.UpdateListenPoint;

import java.util.List;

/**
 * @author zhouson
 * @create 2020-05-21 11:00
 * @desc @CanalEventListener表示开启canal事件监听
 */

@CanalEventListener
public class CanalDataEventListener {
    /**
     * @InsertListenPoint：增加监听
     * @param eventType：当前操作的类型（比如增加数据）
     * @param rowData :发生变更的数据
     * rowData.getAfterColumnsList()：适用于增加、修改
     * rowData.getBeforeColumnsList()：适用于删除
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        //得到增加后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        System.out.println(eventType.getDescriptorForType());
        for(CanalEntry.Column column : afterColumnsList){
            System.out.println("列名："+column.getName()+"---------变更的数据"+column.getValue());
        }
    }


    /**
     * @UpdateListenPoint：修改监听
     * @param eventType：当前操作的类型（比如增加数据）
     * @param rowData :发生变更的数据
     * rowData.getAfterColumnsList()：适用于增加、修改
     * rowData.getBeforeColumnsList()：适用于删除、修改
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("修改前-列名："+column.getName()+"---------变更的数据"+column.getValue());
        }
        for (CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("修改后-列名："+column.getName()+"---------变更的数据"+column.getValue());
        }
    }

    /**
     * @InsertListenPoint：增加监听
     * @param eventType：当前操作的类型（比如增加数据）
     * @param rowData :发生变更的数据
     * rowData.getAfterColumnsList()：适用于增加、修改
     * rowData.getBeforeColumnsList()：适用于删除
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("列名："+column.getName()+"---------变更的数据"+column.getValue());
        }
    }
    /**
     * @ListenPoint：自定义监听
     * eventType：事件类型（增删改查等等），可以指定多个
     * schema:指定监听数据库,可以指定多个
     * table:可以指定多个
     * destination：指定实例的地址，对应配置文件里面的
     */
   /* @ListenPoint(
        eventType={CanalEntry.EventType.DELETE},
            destination="example",
           schema={"changgou_goods"},
            table={"tb_template"}
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("列名："+column.getName()+"=======自定义-变更的数据"+column.getValue());
        }
    }*/
}
