package com.strumenta.antlrplus

import com.strumenta.antlrplus.antlrparser.ANTLRv4Parser

fun ANTLRv4Parser.GrammarSpecContext.toAst(): Grammar {
    return Grammar(this.identifier().text, this.rules().ruleSpec().map { it.toAst() })
}

fun ANTLRv4Parser.RuleSpecContext.toAst(): Rule {
    return when {
        this.lexerRuleSpec() != null -> TODO()
        this.parserRuleSpec() != null -> ParserRule(this.parserRuleSpec().RULE_REF().text, this.parserRuleSpec().ruleBlock().toAst())
        else -> TODO()
    }
}

private fun ANTLRv4Parser.RuleBlockContext.toAst(): RuleBlock {
    return RuleBlock(this.ruleAltList().labeledAlt().map { it.toAst() })
}

private fun ANTLRv4Parser.LabeledAltContext.toAst(): RuleAlternative {
    return RuleAlternative(this.identifier()?.text)
}
