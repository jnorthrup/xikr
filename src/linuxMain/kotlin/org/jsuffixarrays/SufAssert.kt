
package org.jsuffixarrays

actual class SufAssert actual constructor(){
    actual fun sufAssert(function: Boolean, s: () -> Any) {
   assert(function,  {s}) }
}