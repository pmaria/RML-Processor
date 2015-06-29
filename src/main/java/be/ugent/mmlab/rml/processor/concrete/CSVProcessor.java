package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.processor.termmap.TermMapProcessorFactory;
import be.ugent.mmlab.rml.processor.termmap.concrete.ConcreteTermMapFactory;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.QLVocabulary;
import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

/**
 * RML Processor
 *
 * @author mielvandersande, andimou
 */
public class CSVProcessor extends AbstractRMLProcessor {

    // Log
    private static final Logger log = LogManager.getLogger(CSVProcessor.class);
    
    CSVProcessor(){
        TermMapProcessorFactory factory = new ConcreteTermMapFactory();
        this.termMapProcessor = factory.create(QLVocabulary.QLTerm.CSV_CLASS);
    }
    
    private char getDelimiter(LogicalSource ls) {
        String d = RMLEngine.getFileMap().getProperty(ls.getSource() + ".delimiter");
        if (d == null) {
            return ',';
        }
        return d.charAt(0);
    }

    @Override
    public void execute(RMLSesameDataSet dataset, TriplesMap map, RMLPerformer performer, InputStream input) {

        try {
            char delimiter = getDelimiter(map.getLogicalSource());

            //TODO: add charset guessing
            CsvReader reader = new CsvReader(input, Charset.defaultCharset());
            reader.setDelimiter(delimiter);
            
            reader.readHeaders();
            //Iterate the rows
            while (reader.readRecord()) {
                HashMap<String, String> row = new HashMap<>();
               for (String header : reader.getHeaders()) {
                   row.put(new String(header.getBytes("iso8859-1"), UTF_8), reader.get(header));
                }
                //let the performer handle the rows
                performer.perform(row, dataset, map);
            }

        } catch (FileNotFoundException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        } 
    }

    @Override
    public void execute_node(
            RMLSesameDataSet dataset, String expression, TriplesMap parentTriplesMap, 
            RMLPerformer performer, Object node, Resource subject) {
        throw new UnsupportedOperationException("Not applicable for CSV sources."); 
        //TODO: implement this
    }

    @Override
    public String cleansing(String value) {
        return value;
    }
}
