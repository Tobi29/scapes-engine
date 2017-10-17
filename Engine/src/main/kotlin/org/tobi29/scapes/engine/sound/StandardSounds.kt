/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.sound

import org.tobi29.scapes.engine.utils.fromBase64
import org.tobi29.scapes.engine.utils.io.ro
import org.tobi29.scapes.engine.utils.io.tag.TagBundleResource
import org.tobi29.scapes.engine.utils.io.view

// Base64 encoded OGG/Vorbis file
// Original audio by Kenney Vleugels (kenney.nl)
val CLICK by lazy {
    TagBundleResource(
            ("T2dnUwACAAAAAAAAAACNPdRiAAAAAEVrapIBHgF2b3JiaXMAAAAAAUSsAAAAAAAAAHcBAAAAAAC4" +
                    "AU9nZ1MAAAAAAAAAAAAAjT3UYgEAAACxFcVdEC3//////////////////8kDdm9yYmlzHQAAAFhp" +
                    "cGguT3JnIGxpYlZvcmJpcyBJIDIwMDcwNjIyAAAAAAEFdm9yYmlzKUJDVgEACAAAADFMIMWA0JBV" +
                    "AAAQAABgJCkOk2ZJKaWUoSh5mJRISSmllMUwiZiUicUYY4wxxhhjjDHGGGOMIDRkFQAABACAKAmO" +
                    "o+ZJas45ZxgnjnKgOWlOOKcgB4pR4DkJwvUmY26mtKZrbs4pJQgNWQUAAAIAQEghhRRSSCGFFGKI" +
                    "IYYYYoghhxxyyCGnnHIKKqigggoyyCCDTDLppJNOOumoo4466ii00EILLbTSSkwx1VZjrr0GXXxz" +
                    "zjnnnHPOOeecc84JQkNWAQAgAAAEQgYZZBBCCCGFFFKIKaaYcgoyyIDQkFUAACAAgAAAAABHkRRJ" +
                    "sRTLsRzN0SRP8ixREzXRM0VTVE1VVVVVdV1XdmXXdnXXdn1ZmIVbuH1ZuIVb2IVd94VhGIZhGIZh" +
                    "GIZh+H3f933f930gNGQVACABAKAjOZbjKaIiGqLiOaIDhIasAgBkAAAEACAJkiIpkqNJpmZqrmmb" +
                    "tmirtm3LsizLsgyEhqwCAAABAAQAAAAAAKBpmqZpmqZpmqZpmqZpmqZpmqZpmmZZlmVZlmVZlmVZ" +
                    "lmVZlmVZlmVZlmVZlmVZlmVZlmVZlmVZlmVZQGjIKgBAAgBAx3Ecx3EkRVIkx3IsBwgNWQUAyAAA" +
                    "CABAUizFcjRHczTHczzHczxHdETJlEzN9EwPCA1ZBQAAAgAIAAAAAABAMRzFcRzJ0SRPUi3TcjVX" +
                    "cz3Xc03XdV1XVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVYHQkFUAAAQAACGdZpZq" +
                    "gAgzkGEgNGQVAIAAAAAYoQhDDAgNWQUAAAQAAIih5CCa0JrzzTkOmuWgqRSb08GJVJsnuamYm3PO" +
                    "OeecbM4Z45xzzinKmcWgmdCac85JDJqloJnQmnPOeRKbB62p0ppzzhnnnA7GGWGcc85p0poHqdlY" +
                    "m3POWdCa5qi5FJtzzomUmye1uVSbc84555xzzjnnnHPOqV6czsE54Zxzzonam2u5CV2cc875ZJzu" +
                    "zQnhnHPOOeecc84555xzzglCQ1YBAEAAAARh2BjGnYIgfY4GYhQhpiGTHnSPDpOgMcgppB6NjkZK" +
                    "qYNQUhknpXSC0JBVAAAgAACEEFJIIYUUUkghhRRSSCGGGGKIIaeccgoqqKSSiirKKLPMMssss8wy" +
                    "y6zDzjrrsMMQQwwxtNJKLDXVVmONteaec645SGultdZaK6WUUkoppSA0ZBUAAAIAQCBkkEEGGYUU" +
                    "UkghhphyyimnoIIKCA1ZBQAAAgAIAAAA8CTPER3RER3RER3RER3RER3P8RxREiVREiXRMi1TMz1V" +
                    "VFVXdm1Zl3Xbt4Vd2HXf133f141fF4ZlWZZlWZZlWZZlWZZlWZZlCUJDVgEAIAAAAEIIIYQUUkgh" +
                    "hZRijDHHnINOQgmB0JBVAAAgAIAAAAAAR3EUx5EcyZEkS7IkTdIszfI0T/M00RNFUTRNUxVd0RV1" +
                    "0xZlUzZd0zVl01Vl1XZl2bZlW7d9WbZ93/d93/d93/d93/d939d1IDRkFQAgAQCgIzmSIimSIjmO" +
                    "40iSBISGrAIAZAAABACgKI7iOI4jSZIkWZImeZZniZqpmZ7pqaIKhIasAgAAAQAEAAAAAACgaIqn" +
                    "mIqniIrniI4oiZZpiZqquaJsyq7ruq7ruq7ruq7ruq7ruq7ruq7ruq7ruq7ruq7ruq7ruq7rukBo" +
                    "yCoAQAIAQEdyJEdyJEVSJEVyJAcIDVkFAMgAAAgAwDEcQ1Ikx7IsTfM0T/M00RM90TM9VXRFFwgN" +
                    "WQUAAAIACAAAAAAAwJAMS7EczdEkUVIt1VI11VItVVQ9VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" +
                    "VVVVVVVVVVVV1TRN0zSB0JCVAAAZAAAjQQYZhBCKcpBCbj1YCDHmJAWhOQahxBiEpxAzDDkNInSQ" +
                    "QSc9uJI5wwzz4FIoFURMg40lN44gDcKmXEnlOAhCQ1YEAFEAAIAxyDHEGHLOScmgRM4xCZ2UyDkn" +
                    "pZPSSSktlhgzKSWmEmPjnKPSScmklBhLip2kEmOJrQAAgAAHAIAAC6HQkBUBQBQAAGIMUgophZRS" +
                    "zinmkFLKMeUcUko5p5xTzjkIHYTKMQadgxAppRxTzinHHITMQeWcg9BBKAAAIMABACDAQig0ZEUA" +
                    "ECcA4HAkz5M0SxQlSxNFzxRl1xNN15U0zTQ1UVRVyxNV1VRV2xZNVbYlTRNNTfRUVRNFVRVV05ZN" +
                    "VbVtzzRl2VRV3RZV1bZl2xZ+V5Z13zNNWRZV1dZNVbV115Z9X9ZtXZg0zTQ1UVRVTRRV1VRV2zZV" +
                    "17Y1UXRVUVVlWVRVWXZlWfdVV9Z9SxRV1VNN2RVVVbZV2fVtVZZ94XRVXVdl2fdVWRZ+W9eF4fZ9" +
                    "4RhV1dZN19V1VZZ9YdZlYbd13yhpmmlqoqiqmiiqqqmqtm2qrq1bouiqoqrKsmeqrqzKsq+rrmzr" +
                    "miiqrqiqsiyqqiyrsqz7qizrtqiquq3KsrCbrqvrtu8LwyzrunCqrq6rsuz7qizruq3rxnHrujB8" +
                    "pinLpqvquqm6um7runHMtm0co6rqvirLwrDKsu/rui+0dSFRVXXdlF3jV2VZ921fd55b94WybTu/" +
                    "rfvKceu60vg5z28cubZtHLNuG7+t+8bzKz9hOI6lZ5q2baqqrZuqq+uybivDrOtCUVV9XZVl3zdd" +
                    "WRdu3zeOW9eNoqrquirLvrDKsjHcxm8cuzAcXds2jlvXnbKtC31jyPcJz2vbxnH7OuP2daOvDAnH" +
                    "jwAAgAEHAIAAE8pAoSErAoA4AQAGIecUUxAqxSB0EFLqIKRUMQYhc05KxRyUUEpqIZTUKsYgVI5J" +
                    "yJyTEkpoKZTSUgehpVBKa6GU1lJrsabUYu0gpBZKaS2U0lpqqcbUWowRYxAy56RkzkkJpbQWSmkt" +
                    "c05K56CkDkJKpaQUS0otVsxJyaCj0kFIqaQSU0mptVBKa6WkFktKMbYUW24x1hxKaS2kEltJKcYU" +
                    "U20txpojxiBkzknJnJMSSmktlNJa5ZiUDkJKmYOSSkqtlZJSzJyT0kFIqYOOSkkptpJKTKGU1kpK" +
                    "sYVSWmwx1pxSbDWU0lpJKcaSSmwtxlpbTLV1EFoLpbQWSmmttVZraq3GUEprJaUYS0qxtRZrbjHm" +
                    "GkppraQSW0mpxRZbji3GmlNrNabWam4x5hpbbT3WmnNKrdbUUo0txppjbb3VmnvvIKQWSmktlNJi" +
                    "ai3G1mKtoZTWSiqxlZJabDHm2lqMOZTSYkmpxZJSjC3GmltsuaaWamwx5ppSi7Xm2nNsNfbUWqwt" +
                    "xppTS7XWWnOPufVWAADAgAMAQIAJZaDQkJUAQBQAAEGIUs5JaRByzDkqCULMOSepckxCKSlVzEEI" +
                    "JbXOOSkpxdY5CCWlFksqLcVWaykptRZrLQAAoMABACDABk2JxQEKDVkJAEQBACDGIMQYhAYZpRiD" +
                    "0BikFGMQIqUYc05KpRRjzknJGHMOQioZY85BKCmEUEoqKYUQSkklpQIAAAocAAACbNCUWByg0JAV" +
                    "AUAUAABgDGIMMYYgdFQyKhGETEonqYEQWgutddZSa6XFzFpqrbTYQAithdYySyXG1FpmrcSYWisA" +
                    "AOzAAQDswEIoNGQlAJAHAEAYoxRjzjlnEGLMOegcNAgx5hyEDirGnIMOQggVY85BCCGEzDkIIYQQ" +
                    "QuYchBBCCKGDEEIIpZTSQQghhFJK6SCEEEIppXQQQgihlFIKAAAqcAAACLBRZHOCkaBCQ1YCAHkA" +
                    "AIAxSjkHoZRGKcYglJJSoxRjEEpJqXIMQikpxVY5B6GUlFrsIJTSWmw1dhBKaS3GWkNKrcVYa64h" +
                    "pdZirDXX1FqMteaaa0otxlprzbkAANwFBwCwAxtFNicYCSo0ZCUAkAcAgCCkFGOMMYYUYoox55xD" +
                    "CCnFmHPOKaYYc84555RijDnnnHOMMeecc845xphzzjnnHHPOOeecc44555xzzjnnnHPOOeecc845" +
                    "55xzzgkAACpwAAAIsFFkc4KRoEJDVgIAqQAAABFWYowxxhgbCDHGGGOMMUYSYowxxhhjbDHGGGOM" +
                    "McaYYowxxhhjjDHGGGOMMcYYY4wxxhhjjDHGGGOMMcYYY4wxxhhjjDHGGGOMMcYYY4wxxhhjjDHG" +
                    "GFtrrbXWWmuttdZaa6211lprrQBAvwoHAP8HG1ZHOCkaCyw0ZCUAEA4AABjDmHOOOQYdhIYp6KSE" +
                    "DkIIoUNKOSglhFBKKSlzTkpKpaSUWkqZc1JSKiWlllLqIKTUWkottdZaByWl1lJqrbXWOgiltNRa" +
                    "a6212EFIKaXWWostxlBKSq212GKMNYZSUmqtxdhirDGk0lJsLcYYY6yhlNZaazHGGGstKbXWYoy1" +
                    "xlprSam11mKLNdZaCwDgbnAAgEiwcYaVpLPC0eBCQ1YCACEBAARCjDnnnHMQQgghUoox56CDEEII" +
                    "IURKMeYcdBBCCCGEjDHnoIMQQgghhJAx5hx0EEIIIYQQOucchBBCCKGEUkrnHHQQQgghlFBC6SCE" +
                    "EEIIoYRSSikdhBBCKKGEUkopJYQQQgmllFJKKaWEEEIIoYQSSimllBBCCKWUUkoppZQSQgghlFJK" +
                    "KaWUUkIIoZRQSimllFJKCCGEUkoppZRSSgkhhFBKKaWUUkopIYQSSimllFJKKaUAAIADBwCAACPo" +
                    "JKPKImw04cIDUGjISgCADAAAcdhq6ynWyCDFnISWS4SQchBiLhFSijlHsWVIGcUY1ZQxpRRTUmvo" +
                    "nGKMUU+dY0oxw6yUVkookYLScqy1dswBAAAgCAAwECEzgUABFBjIAIADhAQpAKCwwNAxXAQE5BIy" +
                    "CgwKx4Rz0mkDABCEyAyRiFgMEhOqgaJiOgBYXGDIB4AMjY20iwvoMsAFXdx1IIQgBCGIxQEUkICD" +
                    "E2544g1PuMEJOkWlDgIAAAAAAAEAHgAAkg0gIiKaOY4Ojw+QEJERkhKTE5QAAAAAAOABgA8AgCQF" +
                    "iIiIZo6jw+MDJERkhKTE5AQlAAAAAAAAAAAACAgIAAAAAAAEAAAACAhPZ2dTAAQzMwAAAAAAAI09" +
                    "1GICAAAA2eDj+xcm1CgsKjQzMi41MejHrrO3ubu3usKrG/wIVfQ2Q1tNINo57BIpQNPM2TI2L989" +
                    "bb+14yd6+45f+DULsmQM0mV8xyqCgNfUfTrjTRzNmlQe0HXtOB02ASq3WkWASmLUUSsAAEDiMQMH" +
                    "b/uWlcaHn80apqYOHDrwxVdHpKsfWI9/Xv36yOBECCEECt4SqVXaCF9WxtR8mGi65xiT0XRKVILn" +
                    "Kltmu9AK92M6C2LJwtVBIg2v6ZGhKl69hC6rXFQaBQIdN/mQe0X3wKF5N0otopn5Enm4hy0GPrWV" +
                    "7gqbQcmRLBefyCaDrAT7N6dIlF0356qi8aEvJYKMU4eyr2pBSMGlVCLg0y9DBpWB2MG+6/VhbQBM" +
                    "CVMKGneefhHKZnrXSjCCX3OqhSAUdQB7DKpBaWWiq6UXJM7RVPQCPAFxq+dgPbvi/Rj3re2WQ3JJ" +
                    "Tbx7lETpuEa39NIZjWAbcXnc781s04IS4wFUDUlN8JRqr3x00by1ig4YwWuvDnOGSBydeufeCj+h" +
                    "OHnLBkXn1tWVgkNk/UQctBX27RJc80bfblIp/Mjea7Wxzn2eSx3VQUL/rB8WZDcCI9gdfXaNDQ18" +
                    "nzV7ASsBNPV2UJ7vE0aiJEc1/8uCcsycvyqSRtEv+nCLzvuU1pUGfywloPP6yXP7AJ7J9zwLHy8G" +
                    "XPV2mMBmHbmcgNtaA2LgqH9b7Cq1MvemZTTeMRkxcD72w4Jd/JSZnIdhXT2s7djQTgBk/cpl+03h" +
                    "S97k9iEgDbdevL19fjiVfSvJordFxXqP2e2PNIezcUZu98zLH4IN3EnLYRtJGZ84k0Pf90jHXt7f" +
                    "nrdxOeeL/gUdp9OptVN3+tJptJ/g7T/6paRu6OLfZPaSMgG8Ra+lbKNFMVRi/FnAGpGs3Vdloviv" +
                    "p3UN15/as12PSKAe75vNwfwvmtdiqvYRE64AurXbNDwUEASIERhyt8jYgx8AgNUCZFfq6yTPWmut" +
                    "FWuew6XzoQzy5MeVK8P7qcsVtPkMdP04e/W/L68zrKXjqjx+138IBVhp/3NGZlkXN3l3PnfD0YuO" +
                    "CjuqOmFP5f35QwRx+SYA8mOHpbX21QMAhqwzXUGzAGszgXTJb18hYD0/07NWxouA54tFqeFxXZ7N" +
                    "tGCzOg5uBAn3368OADX9KFNTMgPPda8L+k4Ax62Nf7/ck/WcT2CVKTbw30RIcIcpD4PAlCwwmZqS" +
                    "h+ypKUAGTU3t7J7UhDsBIjfW87OAOgllSg4lGwDHAt5FHMRMALnMb8qQyf5D8wfYsGXAEw0I6Lyk" +
                    "ka0SpUkmAQASZUHk+3KTN2KsE8fvXGVJw6j2b3RsCTCs+kkDEh8gYnmFL68aSlVHBXuO6+M/1mD6" +
                    "yU1s5YtAihe4on+HypS4kQJo93sdkeMaOqQTLKF20xoeYyuLbCdEtka798qzfdbOD9l7Z/iht1c0" +
                    "TkgbkeflCdpcLAzH+oQe0RCkJ/XkYBkrwCEGvQ9AGQnNA6Nq2SkxVu8naQpjw8Wr4i5u8XD01/wG" +
                    "AADeNcxIJmUzpOd+S+RKVrgPwE56FPig70nL90jFtmFmCgAAYIy13KFWV5UMnsPU8Oh+NskXc3Wo" +
                    "nhUWfiuWkFomT0lEA7LuB0fYOpq/1P2+haL3GKvIbX/FHbDPCvClCCpQA1MhfFP+FVCf2j7lb8y9" +
                    "t3ZATYR41kGhLH8UQXfnje7h2HTsOUYAwA1/QChoa3TIGovHLaHWEYwNx+HhypcBjjpGYSX921nK" +
                    "Aw2YQAK+VQxq/V8OrzdJuKYsPADDxAXQ9c4A0NbEWstaCAoAAGAPfEzYsMBQVHhimbmeTeUqR606" +
                    "r8Dby1AAUDikye7n82uBYcdwi4cGhIcjV+MvRGm4wtc8A+btejtqo9nJsOK+c91awOwoXWzDtpK4" +
                    "5StiLWtlfeXGGqwL9RR7nM+Kj6Q03mV2K9ToNtjvknF0AHa5AYVraJnqDRGyY1YyARnymTeJWnDh" +
                    "feKVOmliqGQTzsxME95ldEsS/wBApv3zo++mq2AOD8COwIQ1OwAoAdB2vkeyVaSiAAAAqL3dDokW" +
                    "rvfFa6fM/aOHn11a+1kiouUoCvM/FJAigBJaAAkFAMAXcBRNQvIS3L4AYYzqIWsJlO4GAC3ybJrb" +
                    "6P45w6vwWqCvh0W2/ylYotUJENJ45vb1PyZMbIMO7eFyVAFQ47GvgE039UuU7XriPDpG47gFjnvm" +
                    "W9jFzJWdm+abDP5KQ+GesM3smxEuYgwdAL5ldJflPV6l6al1zasBD8fSA8P5wDP4ArpKPeW00kwB" +
                    "AFCSb8Yt7YR2axC0xNYLU+1YliTA+BNgBAAAGvjLnwJgP6L9UAH01WSGdC/5pXQKKiF3cnnlsRwQ" +
                    "ThaP2dmT7hJYBSaDJkktDteodVc8DQg3BaUWTTWrMxlWcoUXeR7qRG/zWPbg2TlxwmQg3FVya0BW" +
                    "p5GtM1oKm0SUKZBl78W2aq17r/axJzWOMMvxIeg920duM+VmCOYG3mVcb6u+WxDG/qfh/dgXDhQg" +
                    "kCW2AgBpJyGi0LIWFAAAcOL7Ygn1IzGZaaIvc74zvQOgrQMAAAB4mgTJczgWgFaXoImBWkG+RcL0" +
                    "cgwDwXMAEnj5y1AMhkbG9lldar2ptZkUkjVxO4tMw4HJ1fxDvUIP6IKmQG+hNKFdVixK9lXnsNJA" +
                    "oo7U/56WeF78KsVrdtViORnrDSVLolpli995NWb7glC160rVCM73u/PMexqCFW35WgdwrqFQAL5l" +
                    "HO/nG6GMH9+O9+1jlAADD4JdDUilukprWWgAfhoX67tMI7nUUdcQobQBAAAAgJftJ++7JdOevXJO" +
                    "gPU/9cJS/BxAtNrSBkFpwmVD66rlrcJafUe0H8laAMTjygrJwnPCGl/kQ3rfg/5FnCD1L2OtdENs" +
                    "F4LupjPOxYs9QUyUqygxPLVugovYa1DrJlTAenefWaG/jixj+Nf0tWBCSWrMdq6GoaKM9g4KT1sz" +
                    "WCoB6aYQqrq4fN5lvNzWbyTBdvDtF4KhQXroEgDVymR50WpNgairtrb655tEP7MRGkAysxQAAAAU" +
                    "wHVb8J7c38E3JSUGGJDp/oQnik8XekHnykWgppEQy/cvjBau9d5jE5HJxCrYFPcSmyS2a9DqZaDv" +
                    "NAcy5m56dsXzGwp73U0FaN8wUuB3ErrBdCY5CT2/qHvsubCZwYQiaUNhiBFKCzKiJi11N8ayoUGW" +
                    "ovlH7aQN4m1Mb+fXXWfXjEJCqPKGTL6BAN5lfF/qN6QiGfj0giSALMAYAFWJl9e+1poCRiB4Dh46" +
                    "65ZTcqqA9P2CAgAAAGiOagNcov2r+bdRINyias5zfSTrzYXU9z7Qic364Ef5ly/zlUwJHDTZi8jh" +
                    "i4A+hIB41BB69OvhSBVQrb14wJ1Mq0ePqjkvVpMuYqnH472ocm7jHwvleFd4/f6u37o9ZuipBFCj" +
                    "CEmLIbAdu13TlKBnfr2VufprkUXGQdIbxWYEnluleCwL1r6NTB+e+CGobx23ThQA3mX8Psu3oUgS" +
                    "Pi0gZnyANI6VyFpoS4Eksd8WAEDp8P1GmBmd7IMDSNOTccRVBZJMqN4C1B/GoYSs1mmzN0oqAFam" +
                    "fFCvtOH7y9ctm2TIotkZnDGl5nopogbHPxeoSNubloNw0LwIthUtoDxjGEIwXaQcI7MjI2OJyWNA" +
                    "w96QU5FjUcySbVEtGEAs7wdYLXFNH8Gxnoc1olBWAmjJ0rBtwqeopABIizC8D5oE3mX8dY7fWIJk" +
                    "Oj4aAEIRAAAAAAAAdXYmAAAA")
                    .fromBase64().view.ro)
}
