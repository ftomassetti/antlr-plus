# antlr-plus

[![Build Status](https://travis-ci.org/ftomassetti/antlr-plus.svg?branch=master)](https://travis-ci.org/ftomassetti/antlr-plus)

Using ANTLR is a great way to define a parser. Once you have a parser you can get an Abstract Syntax Tree. 
Once you have the AST you are on your own. At that point you may want to use antlr-plus to implement the next steps:

* obtaining a metamodel from your parser
* obtain a model from your AST
* transform the model
* add additional information like calculated fields to your model

Antlr-plus has been built to support ANTLR users in performing the typical operation that they want to perform on the AST,
to reshape the data they obtained from the parser and use it more easily.

It is a lightweight alternative to more heavy platforms like [EMF](https://en.wikipedia.org/wiki/Eclipse_Modeling_Framework).

### Introduction

The goal of antlr-plus is taking advantage of all the information available from the ANTLR parser and the AST. You can obtain for free a metamodel and a model.

You have also the possibility to configure the mapping process, from the ANTLR parser to the metamodel and from the AST to the model.

If this is not enough you can simply extends the framework as you wish by implementing the right interface or extending the right class.

### Getting the metamodel

```
// You can get the corresponding definition for your rule in this way
Entity entity = new AntlrReflectionMapper(MyAntlrParser.ruleNames, MyAntlrLexer.class).getEntity(MyAntlrParser.SomeRuleContext.class);
// Once you have an entity you can ask it the properties or the relations to other rules
entity.getProperties();
entity.getRelations();
```

### Getting the model

```
AntlrReflectionMapper refMapper = new AntlrReflectionMapper(MyAntlrParser.ruleNames, MyAntlrLexer.class);
// rootNode is the AST node obtained from ANTLR
OrderedElement element = refMapper.toRootElement(rootNode);
```

### Dumping to XML

```
OrderedElement myElement;
XmlExporter xmlExporter = new XmlExporter();
xmlExporter.toXmlFile(myElement, new File("aFile.xml"));
```

### Maintainer

The creator and the  maintainer of the project is me, Federico Tomassetti. You can read about my thoughts on Language Engineering
at [http://tomassetti.me](http://tomassetti.me)

### License

Antlr-plus is available under the Apache License 2.
