/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 * at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI,
 * you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.ehr.clinical_documents.data

class DvMultimediaIndex extends DataValueIndex {

   String alternateText
   String mediaType // valid iana media types / mime types
   int size // calculated
   byte[] data

   // uri: DV_URI to support references
   String uri

   static constraints = {
      data maxSize: 1073741824  // 1GB
      alternateText nullable: true
      uri nullable: true

      // https://www.sitepoint.com/web-foundations/mime-types-complete-list/
      // https://www.iana.org/assignments/media-types/media-types.xhtml
      // https://github.com/openEHR/terminology/blob/master/openEHR_RM/openehr_external_terminologies.xml#L399-L464
      mediaType inList: [
                        "application/dicom",
                        "application/pdf",
                        "application/rtf",
                        "application/msword",
                        "application/rtf",

                        "image/dicom-rle",
                        "image/jp2",
                        "image/png",
                        "image/jpeg",
                        "image/gif",
                        "image/tiff",

                        "video/H264",
                        "video/mp4",
                        "video/ogg",
                        "video/mpeg",
                        "video/BT656",
	                     "video/CelB",
	                     "video/JPEG",
	                     "video/H261",
	                     "video/H263",
	                     "video/H263-1998",
	                     "video/H263-2000",
                        "video/MPV",
                        "video/quicktime",

                        "audio/mp4",
                        "audio/mpeg",
                        "audio/ogg",
                        "audio/vorbis",
                        "audio/mpeg3",
                        "audio/DVI4",
	                     "audio/G722",
	                     "audio/G723",
	                     "audio/G726-16",
	                     "audio/G726-24",
	                     "audio/G726-32",
	                     "audio/G726-40",
	                     "audio/G728",
	                     "audio/L8",
	                     "audio/L16",
	                     "audio/LPC",
	                     "audio/G729",
	                     "audio/G729D",
                        "audio/G729E"
                        ]
   }

   def beforeInsert()
   {
      this.size = data.length
   }

   def beforeUpdate()
   {
      this.size = data.length
   }

   // true if data is in the EHR, false if it's a reference
   boolean isInline()
   {
      return data.length > 0
   }
}
