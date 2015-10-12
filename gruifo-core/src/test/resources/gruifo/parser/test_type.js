/**
 * A static package level field that can be set.
 * @type {boolean}
 */
nl.test.SOME_STATIC_1 = false;

/**
 * A static package level const field, that can only be read.
 * @const
 * @type {number}
 * @api stable
 */
nl.test.SOME_STATIC_2 = goog.global.devicePixelRatio || 1;
