# antlr-plus

Using ANTLR is a great way to define a parser. Once you have a parser you can get an Abstract Syntax Tree. 
Once you have the AST you are on your own. At that point you may want to use antlr-plus to implement the next steps:

* obtaining a metamodel from your parser
* obtain a model from your AST
* transform the model
* add additional information like calculated fields to your model

Antlr-plus has been built to support ANTLR users in performing the typical operation that they want to perform on the AST,
to reshape the data they obtained from the paerser and use it more easily.

It is a lightweight alternative to more heavy platforms like [EMF](https://en.wikipedia.org/wiki/Eclipse_Modeling_Framework).

### Maintainer

The creator and the  maintainer of the project is me, Federico Tomassetti. You can read about my thoughts on Language Engineering
at [http://tomassetti.me](http://tomassetti.me)

### License

Antlr-plus is available under the Apache License 2.
