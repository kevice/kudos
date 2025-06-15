package io.kudos.base.data.xls

import java.io.File

/**
 * excel数据导入器接口
 *
 * @param T 行对象类型
 * @author
 * @since 1.0.0
 */
interface IExcelImporter<T> {

    /**
     * 执行导入
     *
     * @param xlsFile excel文件
     * @return 导入的行对象列表
     * @throws IllegalStateException 导入过程有错误发生时
     * @author
     * @since 1.0.0
     */
    fun import(xlsFile: File): List<T>

}