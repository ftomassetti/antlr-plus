package me.tomassetti.antlrplus;

import me.tomassetti.antlrplus.antlrparser.ANTLRv4Parser;
import me.tomassetti.antlrplus.antlrparser.Antlr4ParserFacade;
import org.antlr.runtime.RecognitionException;
import org.antlr.v4.parse.*;


import java.io.*;
import java.util.*;

import org.antlr.v4.Tool;

/**
 * Created by federico on 28/05/16.
 */
public class AntlrSimplifier {

    private int nSynRules = 0;
    
    // A rule with only one alternative of one element is transparent
    private Map<String, String> transparentSingleRules = new HashMap<>();
    private Map<String, String> transparentMultipleRules = new HashMap<>();

    // A rule with two or more alternatives every one of them of one element is Abstract
    private Set<String> abstractTypes = new HashSet<>();

    // The rules which have exactly one alternative or exactly one element can be removed

    // I want to find all the rules which are used exactly once as the only element of a top level sequence
    // they can be seen as a sub-class

    // Then I want to recognize lists like : import_as_names:import_as_name(','import_as_name)*','?;

    public void simplify(File file) throws FileNotFoundException {
        simplify(new FileInputStream(file));
    }

    private void analyzeRule(ANTLRv4Parser.RuleSpecContext rule) {
        if (rule.parserRuleSpec() != null) {
            System.out.println("RULE " + rule.getText());
            String name = rule.parserRuleSpec().RULE_REF().getText();
            List<ANTLRv4Parser.LabeledAltContext> topAlternatives = rule.parserRuleSpec().ruleBlock().ruleAltList().labeledAlt();
            //System.out.println("  n alternatives: " + topAlternatives.size());
            if (topAlternatives.size() == 1) {
                List<ANTLRv4Parser.ElementContext> elements = topAlternatives.get(0).alternative().element();
                //System.out.println("  n elements: " + elements);
                if (elements.size() == 1) {
                    String replacedBy = elements.get(0).getText();
                    boolean single = true;
                    if (elements.get(0).ebnfSuffix() != null) {
                        String text = elements.get(0).ebnfSuffix().getText();
                        if (text.equals("*") || text.equals("+")) {
                            single = false;
                        }
                    }
                    if (single) {
                        transparentSingleRules.put(name, replacedBy);
                    } else {
                        transparentMultipleRules.put(name, replacedBy);
                    }
                    System.out.println("  TRANSPARENT "+(single?"single":"multiple"));
                }
            } else if (topAlternatives.size() >= 2) {
                if (topAlternatives.stream().allMatch(alt -> alt.alternative().element().size() == 1)) {
                    abstractTypes.add(name);
                    System.out.println("  ABSTRACT");
                }
            }
            nSynRules++;
        }
    }

    private void analyzeGrammar(ANTLRv4Parser.GrammarSpecContext grammar) {
        grammar.rules().ruleSpec().forEach(r -> analyzeRule(r));
    }

    public void simplify(InputStream inputStream) {
        Antlr4ParserFacade facade = new Antlr4ParserFacade();
        ANTLRv4Parser.GrammarSpecContext grammarSpec = facade.parseStream(inputStream);
        analyzeGrammar(grammarSpec);
        System.out.println("nSynRules "+nSynRules);
        System.out.println("transparentSingleRules "+transparentSingleRules.size());
        System.out.println("transparentMultipleRules "+transparentMultipleRules.size());
        System.out.println("abstractTypes "+abstractTypes.size());
    }

}
