package xyz.junerver.fileselector

/**
 * Description: 对FM进行修改or删除，由页面自行实现
 * @author Junerver
 * date: 2021/12/31-7:49
 * Email: junerver@gmail.com
 * Version: v1.0
 */
interface OperateFileModelItem {
    /**
    * Description: 变更item数据，UI层做重命名等操作后应该调用该函数
    * @author Junerver
    * @Email: junerver@gmail.com
    * @Version: v1.0
    * @param
    * @return
    */
    fun changeItem(fileModel: FileModel)
    //移除item
    fun delItem(fileModel: FileModel)
}