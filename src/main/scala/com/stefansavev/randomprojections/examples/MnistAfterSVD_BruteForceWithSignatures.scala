package com.stefansavev.randomprojections.examples

import java.io.PrintWriter
import com.stefansavev.randomprojections.datarepr.dense.DataFrameOptions
import com.stefansavev.randomprojections.datarepr.dense.DataFrameView
import com.stefansavev.randomprojections.datarepr.dense.DenseRowStoredMatrixViewBuilderFactory
import com.stefansavev.randomprojections.datarepr.dense.RowStoredMatrixView
import com.stefansavev.randomprojections.datarepr.dense.{DataFrameOptions, DataFrameView, DenseRowStoredMatrixViewBuilderFactory, RowStoredMatrixView}
import com.stefansavev.randomprojections.evaluation.Evaluation
import com.stefansavev.randomprojections.file.CSVFileOptions
import com.stefansavev.randomprojections.file.CSVFileOptions
import com.stefansavev.randomprojections.implementation.IndexSettings
import com.stefansavev.randomprojections.implementation.KNNS
import com.stefansavev.randomprojections.implementation.NonThreadSafeSearcher
import com.stefansavev.randomprojections.implementation.ProjectionStrategies
import com.stefansavev.randomprojections.implementation.RandomTrees
import com.stefansavev.randomprojections.implementation.SearcherSettings
import com.stefansavev.randomprojections.implementation.bucketsearch.PointScoreSettings
import com.stefansavev.randomprojections.implementation.bucketsearch.PriorityQueueBasedBucketSearchSettings
import com.stefansavev.randomprojections.implementation.indexing.IndexBuilder
import com.stefansavev.randomprojections.implementation._
import com.stefansavev.randomprojections.implementation.bucketsearch.{PointScoreSettings, PriorityQueueBasedBucketSearchSettings}
import com.stefansavev.randomprojections.implementation.indexing.IndexBuilder
import com.stefansavev.randomprojections.serialization.RandomTreesSerialization
import com.stefansavev.randomprojections.serialization.RandomTreesSerialization
import com.stefansavev.randomprojections.tuning.PerformanceCounters
import com.stefansavev.randomprojections.evaluation.Evaluation
import com.stefansavev.randomprojections.tuning.PerformanceCounters
import com.stefansavev.randomprojections.utils.AllNearestNeighborsForDataset
import com.stefansavev.randomprojections.utils.Utils
import com.stefansavev.randomprojections.utils.{AllNearestNeighborsForDataset, Utils}

object MnistAfterSVD_BruteForceWithSignatures {
  import RandomTreesSerialization.Implicits._

  def loadData(fileName: String): DataFrameView ={
    val opt = CSVFileOptions(onlyTopRecords = None)
    val dataFrameOptions = new DataFrameOptions(labelColumnName = "label", builderFactory = DenseRowStoredMatrixViewBuilderFactory, normalizeVectors = true)
    RowStoredMatrixView.fromFile(fileName, opt, dataFrameOptions)
  }

  def main (args: Array[String]): Unit = {
    val trainFile = Utils.combinePaths(ExamplesSettings.inputDirectory, "mnist/svdpreprocessed/train.csv")
    //val trainFile = Utils.combinePaths(ExamplesSettings.inputDirectory, "mnist/originaldata/train.csv")
    val indexFile = Utils.combinePaths(ExamplesSettings.outputDirectory, "mnist/svdpreprocessed-index")
    val testFile = Utils.combinePaths(ExamplesSettings.inputDirectory, "mnist/svdpreprocessed/test.csv")
    val predictionsOnTestFile = Utils.combinePaths(ExamplesSettings.outputDirectory, "mnist/predictions-test-svd.csv")

    val doTrain = true
    val doSearch = true
    val doTest = false

    val dataset = loadData(trainFile)

    val randomTreeSettings = IndexSettings(
      maxPntsPerBucket=0,
      numTrees=0,
      maxDepth = None,
      projectionStrategyBuilder = ProjectionStrategies.onlySignaturesStrategy(),
      randomSeed = 39393
    )
    println(dataset)

    if (doTrain) {
      val trees = Utils.timed("Create trees", {
        //IndexBuilder.buildWithPreprocessing(64, settings = randomTreeSettings, dataFrameView = dataset)
        IndexBuilder.build(settings = randomTreeSettings, dataFrameView = dataset)
      }).result
      trees.toFile(indexFile)
    }

    if (doSearch){
      val treesFromFile = RandomTrees.fromFile(indexFile)

      val searcherSettings = SearcherSettings (
        bucketSearchSettings = BruteForceSignatureSearch(),
        pointScoreSettings = PointScoreSettings(topKCandidates = 50, rescoreExactlyTopK = 50),
        randomTrees = treesFromFile,
        trainingSet = dataset)

      val searcher = new BruteForceBySignatureSearcher(searcherSettings)

      PerformanceCounters.initialize()
      val allNN = Utils.timed("Search all Nearest neighbors", {
        AllNearestNeighborsForDataset.getTopNearestNeighborsForAllPoints(100, searcher)
      }).result

      PerformanceCounters.report()

      val accuracy = Evaluation.evaluate(dataset.getAllLabels(), allNN, -1, 1)
      if (Math.abs(accuracy - 97.5666) > 0.001){
        throw new IllegalStateException("broke it")
      }
    }

    if (doTest){
      val testDataset = loadData(testFile)
      println(testDataset)
      val treesFromFile = RandomTrees.fromFile(indexFile)

      val searcherSettings = SearcherSettings (
        bucketSearchSettings = PriorityQueueBasedBucketSearchSettings(numberOfRequiredPointsPerTree = 100),
        pointScoreSettings = PointScoreSettings(topKCandidates = 100, rescoreExactlyTopK = 100),
        randomTrees = treesFromFile,
        trainingSet = dataset)

      val searcher = new NonThreadSafeSearcher(searcherSettings)

      PerformanceCounters.initialize()
      val allNN = Utils.timed("Search all Nearest neighbors", {
        AllNearestNeighborsForDataset.getTopNearestNeighborsForAllPointsTestDataset(100, searcher, testDataset)
      }).result
      writePredictionsInKaggleFormat(predictionsOnTestFile, allNN)
      //expected score on kaggle: ?
    }
  }

  def writePredictionsInKaggleFormat(outputFile: String, allKnns: Array[KNNS]): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println("ImageId,Label")
    var i = 0
    while(i < allKnns.length){
      val topNNLabel = allKnns(i).neighbors(0).label
      val pointId = i + 1
      val asStr = s"${pointId},${topNNLabel}"
      writer.println(asStr)
      i += 1
    }
    writer.close()
  }
}