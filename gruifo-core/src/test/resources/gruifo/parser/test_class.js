/**
 * @classdesc
 * Complete JavaScript class example
 *
 * @constructor
 * @extends {java.util.ArrayList}
 * @fires nl.test.Event
 * @param {java.util.ArrayList=} opt_options Options.
 * @api stable
 */
nl.test.SomeClass = function(opt_options) {
}

/**
 * Static field member example.
 * @const
 * @type {number}
 */
nl.test.SomeClass.SOME_ID = 1;

/**
 * Function with no implementation, known use case that override super function.
 * This function should not result in generated java code.
 * @function
 * @return {nl.test.SomeClass} SomeClass.
 * @api stable
 */
nl.test.SomeClass.prototype.getSomething;

/**
 * Method with 3 parameters.
 * @param {number} first first parameter.
 * @param {number} second second parameter.
 * @param {number} third third parameter.
 */
nl.test.SomeClass.prototype.setSomeFunction3 = function(first, second, third) {
  this.set(first, second, third);
};

/**
 * Method with 4 variable parameters.
 * @param {number|string} first first parameter.
 * @param {number|string} second second parameter.
 * @param {int} third third parameter.
 * @param {number|string} fourth fourth parameter.
 */
nl.test.SomeClass.prototype.setSomeFunction2Double = function(first, second, third, fourth) {
  this.set(first, second, third, fourth);
};

/**
 * Method with no parameters, returns a number.
 * @return {number|undefined} returns a number.
 */
nl.test.SomeClass.prototype.getSomeField = function() {
  return this.someField_;
};

/**
 * Method with no parameters, returns a function 
 * @return {function(nl.test.SomeClass): number|null|undefined} some function
 */
nl.test.SomeClass.prototype.getSomeFunction = function() {
  return this.someFunction_;
};

/**
 * Method with function parameter. no return.
 * @param {function(nl.test.SomeClass):number|null|undefined} someFunction
 *     Render order.
 */
nl.test.SomeClass.prototype.setSomeFunction = function(someFunction) {
  this.set(someFunction);
};
