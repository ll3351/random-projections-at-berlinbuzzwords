package com.stefansavev.fuzzysearch;

import com.stefansavev.fuzzysearch.implementation.FuzzySearchIndexWrapper;

import java.util.Iterator;
import java.util.List;

public class FuzzySearchIndex {
    FuzzySearchIndexWrapper wrapper;

    public FuzzySearchIndex(FuzzySearchIndexWrapper wrapper){
        this.wrapper = wrapper;
    }

    public static FuzzySearchIndex open(String fileName){
        return new FuzzySearchIndex(FuzzySearchIndexWrapper.open(fileName));
    }

    public List<FuzzySearchResult> search(int numNeighbors, double[] query){
        double[] normalizedQuery = DataPointVerifier.normalizeDataPointOrFail(query);
        if (wrapper.searcherSettings().randomTrees().trees().length == 0){
            //it's brute force (work around)
            return wrapper.bruteForceSearch(numNeighbors, normalizedQuery);
        }
        return wrapper.getNearestNeighborsByQuery(numNeighbors, normalizedQuery);
    }

    //this will go away
    public List<FuzzySearchResult> bruteForceSearch(int numNeighbors, double[] query) {
        double[] normalizedQuery = DataPointVerifier.normalizeDataPointOrFail(query);
        return wrapper.bruteForceSearch(numNeighbors, normalizedQuery);
    }

    public Iterator<FuzzySearchItem> getItems(){
        return wrapper.getItems();
    }

    public FuzzySearchItem getItemByName(String name){
        return wrapper.getItemByName(name);
    }

    public int getDimension(){
        return wrapper.getDimension();
    }
}

