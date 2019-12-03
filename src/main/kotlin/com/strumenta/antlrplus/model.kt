package com.strumenta.antlrplus

import com.strumenta.kolasu.model.Named
import com.strumenta.kolasu.model.Node
import com.strumenta.kolasu.model.PossiblyNamed

data class Grammar(val identifier: String, val rules: List<Rule>) : Node()

abstract class Rule() : Node(), Named

data class LexerRule(override val name: String) : Rule()

data class ParserRule(override val name: String, val block: RuleBlock) : Rule()

data class RuleBlock(val alternatives: List<RuleAlternative>) : Node()

data class RuleAlternative(override val name: String?) : Node(), PossiblyNamed
