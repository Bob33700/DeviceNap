package com.bluegecko.timelyview.model.core

/**
 * Model class for cubic bezier figure
 */
abstract class Figure protected constructor(//A chained sequence of points P0,P1,P2,P3/0,P1,P2,P3/0,...
    val controlPoints: Array<FloatArray>
)