package org.tobi29.scapes.engine.utils

import kotlin.reflect.KProperty

fun <R, V> accessSynchronized(lock: Any,
                              accessor: R.() -> DelegatedMutableProperty<Any?, V>) =
        object : DelegatedMutableProperty<R, V> {
            override fun getValue(thisRef: R,
                                  property: KProperty<*>) =
                    synchronized(lock) {
                        accessor(thisRef).getValue(thisRef, property)
                    }

            override fun setValue(thisRef: R,
                                  property: KProperty<*>,
                                  value: V) {
                synchronized(lock) {
                    accessor(thisRef).setValue(thisRef, property, value)
                }
            }
        }
