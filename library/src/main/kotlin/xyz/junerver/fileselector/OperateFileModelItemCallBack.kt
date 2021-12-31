package xyz.junerver.fileselector

/**
 * Description: 对FM进行修改&删除时使用的回调函数，由页面自行实现
 * @author Junerver
 * date: 2021/12/31-7:49
 * Email: junerver@gmail.com
 * Version: v1.0
 */
interface OperateFileModelItemCallBack {
    //变更item数据
    fun changeItem(fileModel: FileModel)
    //移除item
    fun delItem(fileModel: FileModel)
}