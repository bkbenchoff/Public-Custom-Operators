/**
 * COPYRIGHT (C) 2015 Alpine Data Labs Inc. All Rights Reserved.
 */

package com.alpine.plugin.samples.ver1_0

import scala.collection.mutable

import com.alpine.plugin.core._
import com.alpine.plugin.core.datasource.OperatorDataSourceManager
import com.alpine.plugin.core.dialog.OperatorDialog
import com.alpine.plugin.core.io._
import com.alpine.plugin.core.io.defaults.HdfsDelimitedTabularDatasetDefault
import com.alpine.plugin.core.spark.utils.SparkRuntimeUtils
import com.alpine.plugin.core.spark.{SparkIOTypedPluginJob, SparkRuntimeWithIOTypedJob}
import com.alpine.plugin.core.utils.HdfsParameterUtils
import org.apache.spark.SparkContext

class HelloWorldInSparkSignature extends OperatorSignature[
  HelloWorldInSparkGUINode,
  HelloWorldInSparkRuntime] {
  def getMetadata(): OperatorMetadata = {
    new OperatorMetadata(
      name = "HelloWorldInSpark",
      category = "Dataset",
      author = "Sung Chung",
      version = 1,
      helpURL = "",
      iconNamePrefix = ""
    )
  }
}

class HelloWorldInSparkGUINode extends OperatorGUINode[
  IONone,
  HdfsDelimitedTabularDataset] {
  override def onPlacement(
    operatorDialog: OperatorDialog,
    operatorDataSourceManager: OperatorDataSourceManager,
    operatorSchemaManager: OperatorSchemaManager): Unit = {

    HdfsParameterUtils.addStandardHdfsOutputParameters(operatorDialog)

    val outputSchema =
      TabularSchema(Array(ColumnDef("HelloWorld", ColumnType.String)))
    operatorSchemaManager.setOutputSchema(outputSchema)
  }
}

class HelloWorldInSparkRuntime extends SparkRuntimeWithIOTypedJob[
  HelloWorldInSparkJob,
  IONone,
  HdfsDelimitedTabularDataset] {}

class HelloWorldInSparkJob extends SparkIOTypedPluginJob[
  IONone,
  HdfsDelimitedTabularDataset] {
  override def onExecution(
    sparkContext: SparkContext,
    appConf: mutable.Map[String, String],
    input: IONone,
    operatorParameters: OperatorParameters,
    listener: OperatorListener): HdfsDelimitedTabularDataset = {
    val sparkUtils = new SparkRuntimeUtils(
      sparkContext
    )

    val outputPathStr =
      HdfsParameterUtils.getOutputPath(operatorParameters)
    val overwrite =
      HdfsParameterUtils.getOverwriteParameterValue(operatorParameters)

    if (overwrite) {
      sparkUtils.deleteFilePathIfExists(outputPathStr)
    }

    val outputSchema =
      TabularSchema(Array(ColumnDef("HelloWorld", ColumnType.String)))
    val rdd = sparkContext.parallelize(List("Hello World"))
    rdd.saveAsTextFile(outputPathStr)
    new HdfsDelimitedTabularDatasetDefault(
      outputPathStr,
      outputSchema,
      "\t",
      "\\",
      "\"",
      false
    )
  }
}
