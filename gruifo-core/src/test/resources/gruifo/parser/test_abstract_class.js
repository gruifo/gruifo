/**
 * JavaScript test of a class with an abstract method
 * @constructor
 * @struct
 * @api
 */
nl.test.SomeClassAbstract = function() {
};

/**
 * @param {number} is a number.
 * @param {function(nl.test.SomeClassAbstract)} callback Callback.
 */
nl.test.SomeClassAbstract.prototype.callSomething = goog.abstractMethod;


/**
 * @param {number} just a number.
 * @return {number}
 */
nl.test.SomeClass.prototype.setSomeActractMethod = goog.abstractMethod;
