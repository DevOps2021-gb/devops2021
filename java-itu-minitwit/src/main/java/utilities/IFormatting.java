package utilities;

import errorhandling.Result;
import model.Tweet;

import java.util.HashMap;
import java.util.List;

public interface IFormatting {
    Result<String> formatDatetime(String timestamp);
    List<Tweet> tweetsFromListOfHashMap(List<HashMap> result);
}
