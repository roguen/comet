/*
//Copyright (c) 2015 Hitachi Data Systems, Inc.
//All Rights Reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License"); you may
//   not use this file except in compliance with the License. You may obtain
//   a copy of the License at
//
//         http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
//   License for the specific language governing permissions and limitations
//   under the License.
//
//Package: COMET::Data Ingestor Service
//Author: Cliff Grimm <cliff.grimm@hds.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r551+
*/
#include <libxml2/libxml/xmlwriter.h>
#include <libxml2/libxml/xmlstring.h>
#include <gdal.h>
#include <gdal_alg.h>
#include <ogr_srs_api.h>
#include <cpl_string.h>

#include "processFile.h"

#define MY_ENCODING "ISO-8859-1"

static int
GDALInfoReportCorner( xmlTextWriterPtr writer, GDALDatasetH hDataset,
                      OGRCoordinateTransformationH hTransform,
                      const char * corner_name,
                      double x, double y, int hddsv3 );


int processFile(const char *inFileName, xmlBufferPtr outBufferPtr) {
	fprintf(stderr, "begin processFile(%s,...,...)\n",inFileName);
    GDALDatasetH        hDataset;
    GDALDriverH         hDriver;
    char                **papszMetadata;
#ifdef NOT_IMPLEMENTED
    GDALRasterBandH		hBand;
    char				**papszExtraMDDomains = NULL;
    int					iMDD, iBand, bApproxStats = TRUE, bStats = TRUE;
    int					bReportHistograms = FALSE;
    int					bComputeChecksum = FALSE;
    int					bShowColorTable = TRUE;
    int					bShowRAT = TRUE;
#endif // NOT_IMPLEMENTED
    char                **papszFileList;
    int					i, retval;
    int					func_retval = EXIT_FAILURE;
	xmlTextWriterPtr writer = NULL;
    OGRCoordinateTransformationH hTransform = NULL;
    double		adfGeoTransform[6];
    const char  *pszProjection = NULL;


	hDataset = GDALOpen( inFileName, GA_ReadOnly);

	if (hDataset == NULL) {
		fprintf(stderr, "Failed to open file to process with GDAL: '%s'\n", inFileName);
		goto cleanup;
	}
	if(strcmp(GDALGetProjectionRef( hDataset ),"")==0 || strlen(GDALGetProjectionRef( hDataset ))==0) {
		fprintf( stderr, "geo spatial file \"%s\" shouldn't have a blank coordinate system:\n%s\n", inFileName, GDALGetProjectionRef( hDataset ) );
		goto cleanup;
	} 
	
	
/*	
	RasterSize != coordinates... invalid test
if( GDALGetRasterXSize(hDataset) < -180 || GDALGetRasterXSize(hDataset) > 180 || GDALGetRasterYSize(hDataset) < -180 || GDALGetRasterYSize(hDataset) > 180) {
		fprintf(stderr, "file may contain coordinates out of bounds");
		fprintf(stderr, "\tx=%d\n",GDALGetRasterXSize(hDataset));

		goto cleanup;
	}*/







	/***
	 * Initialize the "GDALInfo" document XML file.
	 */
	writer = xmlNewTextWriterMemory(outBufferPtr, 0);
	if (NULL == writer) {
		fprintf(stderr, "Failed to create an xmlwriter\n");
		goto cleanup;
	}

	// Format the XML with indents.
	(void)xmlTextWriterSetIndent(writer, 1);
	(void)xmlTextWriterSetIndentString(writer, BAD_CAST "    ");

	retval = xmlTextWriterStartDocument(writer, NULL, MY_ENCODING, NULL);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartDocument failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterStartElement(writer, BAD_CAST "GDALInfo");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	/***
	 *  Process DatasetDriver information
	 */
	hDriver = GDALGetDatasetDriver( hDataset );

#ifdef PROCESSFILE_DEBUG_OUTPUT
	printf( "Driver: %s/%s\n",
            GDALGetDriverShortName( hDriver ),
            GDALGetDriverLongName( hDriver ) );
#endif

	retval = xmlTextWriterStartElement(writer, BAD_CAST "DatasetDriver");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "ShortName",  BAD_CAST GDALGetDriverShortName( hDriver ));
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "LongName",  BAD_CAST GDALGetDriverLongName( hDriver ));
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}

    /* End DatasetDriver */
    retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	/***
	 *  Process FileList information
	 */
	papszFileList = GDALGetFileList( hDataset );
    if( CSLCount(papszFileList) == 0 )
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "Files: none associated\n" );
#endif
    }
    else
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "Files: %s\n", papszFileList[0] );
#endif
    	retval = xmlTextWriterStartElement(writer, BAD_CAST "Files");
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}

    	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "count",  "%d", CSLCount(papszFileList));
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
    		goto cleanup;
    	}

    	retval = xmlTextWriterWriteElement(writer, BAD_CAST "File", BAD_CAST papszFileList[0]);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
    		goto cleanup;
    	}



        for( i = 1; papszFileList[i] != NULL; i++ ) {
#ifdef PROCESSFILE_DEBUG_OUTPUT
            printf( "       %s\n", papszFileList[i] );
#endif
        	retval = xmlTextWriterWriteElement(writer, BAD_CAST "File", BAD_CAST papszFileList[i]);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        }

        /* End FileList */
        retval = xmlTextWriterEndElement(writer);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}
    }

	CSLDestroy( papszFileList );

	/***
	 *  Process RasterSize information
	 */
#ifdef PROCESSFILE_DEBUG_OUTPUT
    printf( "RasterSize is %d, %d\n",
            GDALGetRasterXSize( hDataset ),
            GDALGetRasterYSize( hDataset ) );
#endif

	retval = xmlTextWriterStartElement(writer, BAD_CAST "RasterSize");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "X",  "%d", GDALGetRasterXSize( hDataset ));
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Y",  "%d", GDALGetRasterYSize( hDataset ));
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}

    /* End RasterSize */
    retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	/***
	 *  Process CoordinateSystem information
	 */
    if( GDALGetProjectionRef( hDataset ) != NULL )
    {
        OGRSpatialReferenceH  hSRS;
        char		      *pszProjection;

        pszProjection = (char *) GDALGetProjectionRef( hDataset );

        hSRS = OSRNewSpatialReference(NULL);
        if( OSRImportFromWkt( hSRS, &pszProjection ) == CE_None )
        {
            char	*pszPrettyWkt = NULL;

            OSRExportToPrettyWkt( hSRS, &pszPrettyWkt, FALSE );
//#ifdef PROCESSFILE_DEBUG_OUTPUT
            printf( "***Coordinate System is: \"%s\"\n", pszPrettyWkt );
//#endif

		if(strcmp(pszPrettyWkt,"")==0 || strlen(pszPrettyWkt)==0) {
			printf( "geo spatial file shouldn't have a blank coordinate system:\n%s\n", pszPrettyWkt );
			goto cleanup;
		}

        	retval = xmlTextWriterWriteElement(writer, BAD_CAST "CoordinateSystem",  BAD_CAST pszPrettyWkt);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
            CPLFree( pszPrettyWkt );
        } else {

//#ifdef PROCESSFILE_DEBUG_OUTPUT
            printf( "Coordinate System is \"%s\"\n",
                    GDALGetProjectionRef( hDataset ) );
//#endif


		if(strcmp(GDALGetProjectionRef( hDataset ),"")==0 || strlen(GDALGetProjectionRef( hDataset ))==0) {
			printf( "geo spatial file shouldn't have a blank coordinate system:\n%s\n", GDALGetProjectionRef( hDataset ) );
			goto cleanup;
		} 


        	retval = xmlTextWriterWriteElement(writer, BAD_CAST "CoordinateSystem",  BAD_CAST GDALGetProjectionRef( hDataset ));
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        }

        OSRDestroySpatialReference( hSRS );
    }

    /* -------------------------------------------------------------------- */
    /*      Report Geotransform.                                            */
    /* -------------------------------------------------------------------- */
	if( GDALGetGeoTransform( hDataset, adfGeoTransform ) == CE_None )
	{
    	retval = xmlTextWriterStartElement(writer, BAD_CAST "GeoTransform");
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}

		if( adfGeoTransform[2] == 0.0 && adfGeoTransform[4] == 0.0 )
		{
#ifdef PROCESSFILE_DEBUG_OUTPUT
			printf( "Origin = (%.15f,%.15f)\n",
					adfGeoTransform[0], adfGeoTransform[3] );

			printf( "Pixel Size = (%.15f,%.15f)\n",
					adfGeoTransform[1], adfGeoTransform[5] );
#endif
        	retval = xmlTextWriterStartElement(writer, BAD_CAST "Origin");
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "X",  "%.15f", adfGeoTransform[0]);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Y",  "%.15f", adfGeoTransform[3]);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
            retval = xmlTextWriterEndElement(writer);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}

        	retval = xmlTextWriterStartElement(writer, BAD_CAST "PixelSize");
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "X",  "%.15f", adfGeoTransform[1]);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Y",  "%.15f", adfGeoTransform[5]);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
            retval = xmlTextWriterEndElement(writer);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
		}
		else {
#ifdef PROCESSFILE_DEBUG_OUTPUT
			printf( "GeoTransform =\n"
					"  %.16g, %.16g, %.16g\n"
					"  %.16g, %.16g, %.16g\n",
					adfGeoTransform[0],
					adfGeoTransform[1],
					adfGeoTransform[2],
					adfGeoTransform[3],
					adfGeoTransform[4],
					adfGeoTransform[5] );
#endif
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "count",  "%d", 6);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}

			// Dump out the elements.
        	int i;
			for (i = 0; i <= 5; i++) {
				retval = xmlTextWriterWriteFormatElement(writer, BAD_CAST "Value", "%.16g", adfGeoTransform[i] );
				if (retval < 0) {
					fprintf(stderr, "xmlTextWriterWriteFormatElement failure: retval = %d\n", retval);
					goto cleanup;
				}
			}
		}

        /* End GeoTransform */
        retval = xmlTextWriterEndElement(writer);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}
	}

	/***
	 *  Process GCPProjectionData information
	 */
	if( GDALGetGCPCount( hDataset ) > 0 )
	{
    	retval = xmlTextWriterStartElement(writer, BAD_CAST "GCPData");
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}

		if (GDALGetGCPProjection(hDataset) != NULL)
		{
			OGRSpatialReferenceH  hSRS;
			char		      *pszProjection;

			pszProjection = (char *) GDALGetGCPProjection( hDataset );

			hSRS = OSRNewSpatialReference(NULL);
			if( OSRImportFromWkt( hSRS, &pszProjection ) == CE_None )
			{
				char	*pszPrettyWkt = NULL;

				OSRExportToPrettyWkt( hSRS, &pszPrettyWkt, FALSE );
#ifdef PROCESSFILE_DEBUG_OUTPUT
				printf( "GCP Projection = \n%s\n", pszPrettyWkt );
#endif
		    	retval = xmlTextWriterWriteElement(writer, BAD_CAST "GCPProjection", BAD_CAST pszPrettyWkt);
		    	if (retval < 0) {
		    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		    		goto cleanup;
		    	}

				CPLFree( pszPrettyWkt );
			} else {
#ifdef PROCESSFILE_DEBUG_OUTPUT
				printf( "GCP Projection = %s\n",
						GDALGetGCPProjection( hDataset ) );
#endif
	        	retval = xmlTextWriterWriteElement(writer, BAD_CAST "GCPProjection",  BAD_CAST GDALGetGCPProjection( hDataset ));
	        	if (retval < 0) {
	        		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
	        		goto cleanup;
	        	}
			}    if (NULL != writer)


			OSRDestroySpatialReference( hSRS );
		}

		for( i = 0; i < GDALGetGCPCount(hDataset); i++ )
		{
			const GDAL_GCP	*psGCP;

	    	retval = xmlTextWriterStartElement(writer, BAD_CAST "GCP");
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}

			psGCP = GDALGetGCPs( hDataset ) + i;

#ifdef PROCESSFILE_DEBUG_OUTPUT
			printf( "GCP[%3d]: Id=%s, Info=%s\n"
					"          (%.15g,%.15g) -> (%.15g,%.15g,%.15g)\n",
					i, psGCP->pszId, psGCP->pszInfo,
					psGCP->dfGCPPixel, psGCP->dfGCPLine,
					psGCP->dfGCPX, psGCP->dfGCPY, psGCP->dfGCPZ );
#endif

        	retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "Id",  BAD_CAST psGCP->pszId);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}

        	retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "Info", BAD_CAST psGCP->pszInfo);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}

        	// Write Raster X, Y position.
	    	retval = xmlTextWriterStartElement(writer, BAD_CAST "Raster");
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}

        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "X",  "%.15g", psGCP->dfGCPPixel);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Y",  "%.15g", psGCP->dfGCPLine);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
			// End Raster
	        retval = xmlTextWriterEndElement(writer);
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}

        	// Write GeoReference X, Y, Z
	    	retval = xmlTextWriterStartElement(writer, BAD_CAST "GeoReference");
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}

        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "X",  "%.15g", psGCP->dfGCPX);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Y",  "%.15g", psGCP->dfGCPY);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "Z",  "%.15g", psGCP->dfGCPZ);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
        		goto cleanup;
        	}
			// End GeoReference
	        retval = xmlTextWriterEndElement(writer);
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}

			/* End GCP Items */
	        retval = xmlTextWriterEndElement(writer);
	    	if (retval < 0) {
	    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
	    		goto cleanup;
	    	}
		}

    	/* End GCPData */
        retval = xmlTextWriterEndElement(writer);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}
	}

	/***
	 *  Process Metadata information
	 */
    papszMetadata = GDALGetMetadata( hDataset, NULL );
    if( CSLCount(papszMetadata) > 0 )
    {
    	retval = xmlTextWriterStartElement(writer, BAD_CAST "Metadata");
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}

    	// First let's count the number of metadata entries for the count attribute.
    	for( i = 0; papszMetadata[i] != NULL; i++ );
    	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "count",  "%d", i);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
    		goto cleanup;
    	}

#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "Metadata:\n" );
#endif
        char name[200];
	char value[200];
	// = malloc(200), *value = malloc(200);
        //int name_buf_size = 0, value_buf_size = 0;
	//fprintf(stderr,"\tname and value preallocated to be huge");
	
	
	
	
        for( i = 0; papszMetadata[i] != NULL; i++ )
        {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        	printf( "  %s\n", papszMetadata[i] );
#endif

        	// Split up the attribute information.
        	int namelen = 0, valuelen = 0;
        	char *equalpos = NULL;

        	equalpos = (char *)strchr(papszMetadata[i], '=');
        	namelen = (NULL == equalpos) ? strlen(papszMetadata[i]) : (int)(equalpos - papszMetadata[i]);
        	valuelen = (NULL == equalpos) ? 0 : strlen(papszMetadata[i]) - (namelen + 1);

        	// Allocate/Reallocate memory if it is bigger.
		//if (NULL == name || name_buf_size < namelen) {
            	//	fprintf(stderr, "\treallocated memory for name\n");
		//	//name = realloc(name, namelen+1);
            	//	name_buf_size = namelen+1;
        	//}

        	//if (NULL == value || value_buf_size < valuelen) {
            	//	fprintf(stderr, "\treallocated memory for value\n");
        	//	value = realloc(value, valuelen+1);
            	//	value_buf_size = valuelen+1;
        	//}

        	//if (NULL == name || NULL == value) {
        	//	fprintf(stderr, "memory allocation failure for metadata holders");
        	//	goto cleanup;
        	//}

        	strncpy(name, papszMetadata[i], namelen);
        	name[namelen] = '\0';

        	if (0 != valuelen) {
        		strncpy(value, papszMetadata[i] + namelen + 1, valuelen);
        	}
    		value[valuelen] = '\0';

        	retval = xmlTextWriterWriteElement(writer, BAD_CAST name, BAD_CAST value);
        	if (retval < 0) {
        		fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
        		goto cleanup;
        	}
        }

    	// Free up our memory.
    	//free(name);
    	//name = NULL;
    	//free(value);
    	//value = NULL;
	
	
	fprintf(stderr,"(from gdalnative) filename=%s\n",inFileName);
	
	
	
	
	

        /* End Metadata */
        retval = xmlTextWriterEndElement(writer);
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}
    }


#ifdef NOT_IMPLEMENTED
    for( iMDD = 0; iMDD < CSLCount(papszExtraMDDomains); iMDD++ )
    {
        papszMetadata = GDALGetMetadata( hDataset, papszExtraMDDomains[iMDD] );
        if( CSLCount(papszMetadata) > 0 )
        {
            printf( "Metadata (%s):\n", papszExtraMDDomains[iMDD]);
            for( i = 0; papszMetadata[i] != NULL; i++ )
            {
                printf( "  %s\n", papszMetadata[i] );
            }
        }
    }
#endif // NOT_IMPLEMENTED

    /* -------------------------------------------------------------------- */
    /*      Report "IMAGE_STRUCTURE" metadata.                              */
    /* -------------------------------------------------------------------- */
	papszMetadata = GDALGetMetadata( hDataset, "IMAGE_STRUCTURE" );
	if( CSLCount(papszMetadata) > 0 )
	{
#ifdef PROCESSFILE_DEBUG_OUTPUT
		printf( "Image Structure Metadata:\n" );
#endif
		retval = xmlTextWriterStartElement(writer, BAD_CAST "ImageStructure");
		if (retval < 0) {
			fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
			goto cleanup;
		}

		retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "count",  "%d", CSLCount(papszMetadata));
		if (retval < 0) {
			fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
			goto cleanup;
		}

		for( i = 0; papszMetadata[i] != NULL; i++ )
		{
#ifdef PROCESSFILE_DEBUG_OUTPUT
			printf( "  %s\n", papszMetadata[i] );
#endif
			// Split up the attribute information.
			char *name, *value;
			name = papszMetadata[i];
			value = (char *)strchr(papszMetadata[i], '=');
			*value = '\0';
			value++;

			retval = xmlTextWriterWriteElement(writer, BAD_CAST name, BAD_CAST value);
			if (retval < 0) {
				fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
				goto cleanup;
			}
		}

		/* End Image_Structure Metadata */
		retval = xmlTextWriterEndElement(writer);
		if (retval < 0) {
			fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
			goto cleanup;
		}
	}

#ifdef NOT_IMPLEMENTED
	/* -------------------------------------------------------------------- */
	/*      Report subdatasets.                                             */
	/* -------------------------------------------------------------------- */
	papszMetadata = GDALGetMetadata( hDataset, "SUBDATASETS" );
	if( CSLCount(papszMetadata) > 0 )
	{
		printf( "Subdatasets:\n" );
		for( i = 0; papszMetadata[i] != NULL; i++ )
		{
			printf( "  %s\n", papszMetadata[i] );
		}
	}

	/* -------------------------------------------------------------------- */
	/*      Report geolocation.                                             */
	/* -------------------------------------------------------------------- */
	papszMetadata = GDALGetMetadata( hDataset, "GEOLOCATION" );
	if( CSLCount(papszMetadata) > 0 )
	{
		printf( "Geolocation:\n" );
		for( i = 0; papszMetadata[i] != NULL; i++ )
		{
			printf( "  %s\n", papszMetadata[i] );
		}
	}

	/* -------------------------------------------------------------------- */
	/*      Report RPCs                                                     */
	/* -------------------------------------------------------------------- */
	papszMetadata = GDALGetMetadata( hDataset, "RPC" );
	if( CSLCount(papszMetadata) > 0 )
	{
		printf( "RPC Metadata:\n" );
		for( i = 0; papszMetadata[i] != NULL; i++ )
		{
			printf( "  %s\n", papszMetadata[i] );
		}
	}

#endif // NOT_IMPLEMENTED

	/* -------------------------------------------------------------------- */
	/*      Setup projected to lat/long transform if appropriate.           */
	/* -------------------------------------------------------------------- */
	if( GDALGetGeoTransform( hDataset, adfGeoTransform ) == CE_None )
		pszProjection = GDALGetProjectionRef(hDataset);

	if( pszProjection != NULL && strlen(pszProjection) > 0 )
	{
		OGRSpatialReferenceH hProj, hLatLong = NULL;

		hProj = OSRNewSpatialReference( pszProjection );
		if( hProj != NULL )
			hLatLong = OSRCloneGeogCS( hProj );

		if( hLatLong != NULL )
		{
			CPLPushErrorHandler( CPLQuietErrorHandler );
			hTransform = OCTNewCoordinateTransformation( hProj, hLatLong );
			CPLPopErrorHandler();

			OSRDestroySpatialReference( hLatLong );
		}

		if( hProj != NULL )
			OSRDestroySpatialReference( hProj );
	}


	/* -------------------------------------------------------------------- */
	/*      Report corners.                                                 */
	/* -------------------------------------------------------------------- */
#ifdef PROCESSFILE_DEBUG_OUTPUT
	printf( "Corner Coordinates:\n" );
#endif
	retval = xmlTextWriterStartElement(writer, BAD_CAST "CornerCoordinates");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		goto cleanup;
	}
	
	
	

	GDALInfoReportCorner( writer, hDataset, hTransform, "Upper Left",
						  0.0, 0.0, 2 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Lower Left",
						  0.0, GDALGetRasterYSize(hDataset), 2);
	GDALInfoReportCorner( writer, hDataset, hTransform, "Upper Right",
						  GDALGetRasterXSize(hDataset), 0.0, 2 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Lower Right",
						  GDALGetRasterXSize(hDataset),
						  GDALGetRasterYSize(hDataset), 2 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Center",
						  GDALGetRasterXSize(hDataset)/2.0,
						  GDALGetRasterYSize(hDataset)/2.0, 2 );

	retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}
	
	
	
	/*do it again for hddsv3*/
	retval = xmlTextWriterStartElement(writer, BAD_CAST "NEOCornerCoordinates");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	GDALInfoReportCorner( writer, hDataset, hTransform, "Upper Left",
						  0.0, 0.0, 3 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Lower Left",
						  0.0, GDALGetRasterYSize(hDataset), 3);
	GDALInfoReportCorner( writer, hDataset, hTransform, "Upper Right",
						  GDALGetRasterXSize(hDataset), 0.0, 3 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Lower Right",
						  GDALGetRasterXSize(hDataset),
						  GDALGetRasterYSize(hDataset), 3 );
	GDALInfoReportCorner( writer, hDataset, hTransform, "Center",
						  GDALGetRasterXSize(hDataset)/2.0,
						  GDALGetRasterYSize(hDataset)/2.0, 3 );

	retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	if( hTransform != NULL )
	{
		OCTDestroyCoordinateTransformation( hTransform );
		hTransform = NULL;
	}

#ifdef NOT_IMPLEMENTED
	/* ==================================================================== */
	/*      Loop over bands.                                                */
	/* ==================================================================== */
	for( iBand = 0; iBand < GDALGetRasterCount( hDataset ); iBand++ )
	{
		double      dfMin, dfMax, adfCMinMax[2], dfNoData;
		int         bGotMin, bGotMax, bGotNodata, bSuccess;
		int         nBlockXSize, nBlockYSize, nMaskFlags;
		double      dfMean, dfStdDev;
		GDALColorTableH hTable;
		CPLErr      eErr;

		hBand = GDALGetRasterBand( hDataset, iBand+1 );

		float afSample[10000];
		int   nCount;

		nCount = GDALGetRandomRasterSample( hBand, 10000, afSample );
		printf( "Got %d samples.\n", nCount );

		GDALGetBlockSize( hBand, &nBlockXSize, &nBlockYSize );
		printf( "Band %d Block=%dx%d Type=%s, ColorInterp=%s\n", iBand+1,
				nBlockXSize, nBlockYSize,
				GDALGetDataTypeName(
					GDALGetRasterDataType(hBand)),
				GDALGetColorInterpretationName(
					GDALGetRasterColorInterpretation(hBand)) );

		if( GDALGetDescription( hBand ) != NULL
			&& strlen(GDALGetDescription( hBand )) > 0 )
			printf( "  Description = %s\n", GDALGetDescription(hBand) );

		dfMin = GDALGetRasterMinimum( hBand, &bGotMin );
		dfMax = GDALGetRasterMaximum( hBand, &bGotMax );
		if( bGotMin || bGotMax )
		{
			printf( "  " );
			if( bGotMin )
				printf( "Min=%.3f ", dfMin );
			if( bGotMax )
				printf( "Max=%.3f ", dfMax );

			CPLErrorReset();
			GDALComputeRasterMinMax( hBand, FALSE, adfCMinMax );
			if (CPLGetLastErrorType() == CE_None)
			{
			  printf( "  Computed Min/Max=%.3f,%.3f",
					  adfCMinMax[0], adfCMinMax[1] );
			}

			printf( "\n" );
		}

		eErr = GDALGetRasterStatistics( hBand, bApproxStats, bStats,
										&dfMin, &dfMax, &dfMean, &dfStdDev );
		if( eErr == CE_None )
		{
			printf( "  Minimum=%.3f, Maximum=%.3f, Mean=%.3f, StdDev=%.3f\n",
					dfMin, dfMax, dfMean, dfStdDev );
		}

		if( bReportHistograms )
		{
			int nBucketCount, *panHistogram = NULL;

			eErr = GDALGetDefaultHistogram( hBand, &dfMin, &dfMax,
											&nBucketCount, &panHistogram,
											TRUE, GDALTermProgress, NULL );
			if( eErr == CE_None )
			{
				int iBucket;

				printf( "  %d buckets from %g to %g:\n  ",
						nBucketCount, dfMin, dfMax );
				for( iBucket = 0; iBucket < nBucketCount; iBucket++ )
					printf( "%d ", panHistogram[iBucket] );
				printf( "\n" );
				CPLFree( panHistogram );
			}
		}

		if ( bComputeChecksum)
		{
			printf( "  Checksum=%d\n",
					GDALChecksumImage(hBand, 0, 0,
									  GDALGetRasterXSize(hDataset),
									  GDALGetRasterYSize(hDataset)));
		}

		dfNoData = GDALGetRasterNoDataValue( hBand, &bGotNodata );
		if( bGotNodata )
		{
			if (CPLIsNan(dfNoData))
				printf( "  NoData Value=nan\n" );
			else
				printf( "  NoData Value=%.18g\n", dfNoData );
		}

		if( GDALGetOverviewCount(hBand) > 0 )
		{
			int         iOverview;

			printf( "  Overviews: " );
			for( iOverview = 0;
				 iOverview < GDALGetOverviewCount(hBand);
				 iOverview++ )
			{
				GDALRasterBandH hOverview;
				const char *pszResampling = NULL;

				if( iOverview != 0 )
					printf( ", " );

				hOverview = GDALGetOverview( hBand, iOverview );
				if (hOverview != NULL)
				{
					printf( "%dx%d",
							GDALGetRasterBandXSize( hOverview ),
							GDALGetRasterBandYSize( hOverview ) );

					pszResampling =
						GDALGetMetadataItem( hOverview, "RESAMPLING", "" );

					if( pszResampling != NULL
						&& EQUALN(pszResampling,"AVERAGE_BIT2",12) )
						printf( "*" );
				}
				else
					printf( "(null)" );
			}
			printf( "\n" );

			if ( bComputeChecksum)
			{
				printf( "  Overviews checksum: " );
				for( iOverview = 0;
					iOverview < GDALGetOverviewCount(hBand);
					iOverview++ )
				{
					GDALRasterBandH     hOverview;

					if( iOverview != 0 )
						printf( ", " );

					hOverview = GDALGetOverview( hBand, iOverview );
					if (hOverview)
						printf( "%d",
								GDALChecksumImage(hOverview, 0, 0,
										GDALGetRasterBandXSize(hOverview),
										GDALGetRasterBandYSize(hOverview)));
					else
						printf( "(null)" );
				}
				printf( "\n" );
			}
		}

		if( GDALHasArbitraryOverviews( hBand ) )
		{
			printf( "  Overviews: arbitrary\n" );
		}

		nMaskFlags = GDALGetMaskFlags( hBand );
		if( (nMaskFlags & (GMF_NODATA|GMF_ALL_VALID)) == 0 )
		{
			GDALRasterBandH hMaskBand = GDALGetMaskBand(hBand) ;

			printf( "  Mask Flags: " );
			if( nMaskFlags & GMF_PER_DATASET )
				printf( "PER_DATASET " );
			if( nMaskFlags & GMF_ALPHA )
				printf( "ALPHA " );
			if( nMaskFlags & GMF_NODATA )
				printf( "NODATA " );
			if( nMaskFlags & GMF_ALL_VALID )
				printf( "ALL_VALID " );
			printf( "\n" );

			if( hMaskBand != NULL &&
				GDALGetOverviewCount(hMaskBand) > 0 )
			{
				int             iOverview;

				printf( "  Overviews of mask band: " );
				for( iOverview = 0;
					 iOverview < GDALGetOverviewCount(hMaskBand);
					 iOverview++ )
				{
					GDALRasterBandH     hOverview;

					if( iOverview != 0 )
						printf( ", " );

					hOverview = GDALGetOverview( hMaskBand, iOverview );
					printf( "%dx%d",
							GDALGetRasterBandXSize( hOverview ),
							GDALGetRasterBandYSize( hOverview ) );
				}
				printf( "\n" );
			}
		}

		if( strlen(GDALGetRasterUnitType(hBand)) > 0 )
		{
			printf( "  Unit Type: %s\n", GDALGetRasterUnitType(hBand) );
		}

		if( GDALGetRasterCategoryNames(hBand) != NULL )
		{
			char **papszCategories = GDALGetRasterCategoryNames(hBand);
			int i;

			printf( "  Categories:\n" );
			for( i = 0; papszCategories[i] != NULL; i++ )
				printf( "    %3d: %s\n", i, papszCategories[i] );
		}

		if( GDALGetRasterScale( hBand, &bSuccess ) != 1.0
			|| GDALGetRasterOffset( hBand, &bSuccess ) != 0.0 )
			printf( "  Offset: %.15g,   Scale:%.15g\n",
					GDALGetRasterOffset( hBand, &bSuccess ),
					GDALGetRasterScale( hBand, &bSuccess ) );

		papszMetadata = GDALGetMetadata( hBand, NULL );
		if( CSLCount(papszMetadata) > 0 )
		{
			printf( "  Metadata:\n" );
			for( i = 0; papszMetadata[i] != NULL; i++ )
			{
				printf( "    %s\n", papszMetadata[i] );
			}
		}

		papszMetadata = GDALGetMetadata( hBand, "IMAGE_STRUCTURE" );
		if( CSLCount(papszMetadata) > 0 )
		{
			printf( "  Image Structure Metadata:\n" );
			for( i = 0; papszMetadata[i] != NULL; i++ )
			{
				printf( "    %s\n", papszMetadata[i] );
			}
		}

		if( GDALGetRasterColorInterpretation(hBand) == GCI_PaletteIndex
			&& (hTable = GDALGetRasterColorTable( hBand )) != NULL )
		{
			int                 i;

			printf( "  Color Table (%s with %d entries)\n",
					GDALGetPaletteInterpretationName(
						GDALGetPaletteInterpretation( hTable )),
					GDALGetColorEntryCount( hTable ) );

			if (bShowColorTable)
			{
				for( i = 0; i < GDALGetColorEntryCount( hTable ); i++ )
				{
					GDALColorEntry      sEntry;

					GDALGetColorEntryAsRGB( hTable, i, &sEntry );
					printf( "  %3d: %d,%d,%d,%d\n",
							i,
							sEntry.c1,
							sEntry.c2,
							sEntry.c3,
							sEntry.c4 );
				}
			}
		}

		if( bShowRAT && GDALGetDefaultRAT( hBand ) != NULL )
		{
			GDALRasterAttributeTableH hRAT = GDALGetDefaultRAT( hBand );

			GDALRATDumpReadable( hRAT, NULL );
		}
	}

#endif // NOT_IMPLEMENTED

    /* End GDALInfo */
    retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}

    retval = xmlTextWriterEndDocument(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndDocument failure: retval = %d\n", retval);
		goto cleanup;
	}

    retval = xmlTextWriterFlush(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterFlush failure: retval = %d\n", retval);
		goto cleanup;
	}

	func_retval = EXIT_SUCCESS;

cleanup:
    if (NULL != writer)
    	xmlFreeTextWriter(writer);

	if (NULL != hDataset)
		GDALClose( hDataset );

#ifdef NOT_IMPLEMENTED
	if (NULL != papszExtraMDDomains)
		CSLDestroy(papszExtraMDDomains );

	GDALDumpOpenDatasets( stderr );

#endif // NOT_IMPLEMENTED


	return func_retval;
}


int WriteXMLDouble( xmlTextWriterPtr writer, const char *name, const char *format, double value, int hddsver) {
	int funcRetVal = FALSE;
	int retval;

	retval = xmlTextWriterStartElement(writer, BAD_CAST name);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		return FALSE;
	}

	if(hddsver==3)
		retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "dataType",  BAD_CAST "number");
	else
		retval = xmlTextWriterWriteAttribute(writer, BAD_CAST "esp-internal-type",  BAD_CAST "float");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterWriteFormatString(writer, format, value);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteFormatElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}

	funcRetVal = TRUE;

cleanup:

	return funcRetVal;

}

/************************************************************************/
/*                        GDALInfoReportCorner()                        */
/************************************************************************/

static int
GDALInfoReportCorner( xmlTextWriterPtr writer,
		              GDALDatasetH hDataset,
                      OGRCoordinateTransformationH hTransform,
                      const char * corner_name,
                      double x, double y, int hddsver )

{
    double	dfGeoX, dfGeoY;
    double	adfGeoTransform[6];
    int retval;
    int funcRetVal = FALSE;

#ifdef PROCESSFILE_DEBUG_OUTPUT
    printf( "%-11s ", corner_name );
#endif
	retval = xmlTextWriterStartElement(writer, BAD_CAST "Corner");
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterStartElement failure: retval = %d\n", retval);
		return FALSE;
	}

	retval = xmlTextWriterWriteFormatAttribute(writer, BAD_CAST "name",  "%s", corner_name);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterWriteFormatAttribute failure: retval = %d\n", retval);
		goto cleanup;
	}


/* -------------------------------------------------------------------- */
/*      Transform the point into georeferenced coordinates.             */
/* -------------------------------------------------------------------- */
    if( GDALGetGeoTransform( hDataset, adfGeoTransform ) == CE_None )
    {
        dfGeoX = adfGeoTransform[0] + adfGeoTransform[1] * x
            + adfGeoTransform[2] * y;
        dfGeoY = adfGeoTransform[3] + adfGeoTransform[4] * x
            + adfGeoTransform[5] * y;
    }

    else
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "(%7.1f,%7.1f)\n", x, y );
#endif
        if ( ! WriteXMLDouble(writer, "X", "%.1f", x, hddsver) )
        	goto cleanup;

        if ( ! WriteXMLDouble(writer, "Y", "%.1f", y, hddsver) )
        	goto cleanup;

    	goto cleanup;
    }

/* -------------------------------------------------------------------- */
/*      Report the georeferenced coordinates.                           */
/* -------------------------------------------------------------------- */
    if( ABS(dfGeoX) < 181 && ABS(dfGeoY) < 91 )
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "(%12.7f,%12.7f) ", dfGeoX, dfGeoY );
#endif
        if ( ! WriteXMLDouble(writer, "X", "%.7f", dfGeoX, hddsver) )
        	goto cleanup;

        if ( ! WriteXMLDouble(writer, "Y", "%.7f", dfGeoY, hddsver) )
        	goto cleanup;

    }
    else
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "(%12.3f,%12.3f) ", dfGeoX, dfGeoY );
#endif
        if ( ! WriteXMLDouble(writer, "X", "%.3f", dfGeoX, hddsver) )
        	goto cleanup;

        if ( ! WriteXMLDouble(writer, "Y", "%.3f", dfGeoY, hddsver) )
        	goto cleanup;
    }

#if 0 // Not outputting Lat/Long because it has quotes in it
/* --------------------------- */
/*      Transform to latlong and report.                                */
/* -------------------------------------------------------------------- */
    if( hTransform != NULL
        && OCTTransform(hTransform,1,&dfGeoX,&dfGeoY,NULL) )
    {
#ifdef PROCESSFILE_DEBUG_OUTPUT
        printf( "(%s,", GDALDecToDMS( dfGeoX, "Long", 2 ) );
        printf( "%s)", GDALDecToDMS( dfGeoY, "Lat", 2 ) );
#endif

	if( dfGeoX < -180.0 || dfGeoX > 180.0 || dfGeoY < -180.0 || dfGeoY > 180.0) {
		fprintf(stderr, "file contains coordinates out of bounds");
		goto cleanup;
	} else {
	
	
		fprintf(stderr, "X=%f, Y=%f",dfGeoX, dfGeoY);
	
	}




    	retval = xmlTextWriterWriteElement(writer, BAD_CAST "Longitude", BAD_CAST GDALDecToDMS( dfGeoX, "Long", 2 ));
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}

    	retval = xmlTextWriterWriteElement(writer, BAD_CAST "Latitude", BAD_CAST GDALDecToDMS( dfGeoY, "Lat", 2 ));
    	if (retval < 0) {
    		fprintf(stderr, "xmlTextWriterWriteElement failure: retval = %d\n", retval);
    		goto cleanup;
    	}
    }
#endif // Not outputting Long/Lat

#ifdef PROCESSFILE_DEBUG_OUTPUT
    printf( "\n" );
#endif

	funcRetVal = TRUE;

cleanup:
	/* End Image_Structure Metadata */
	retval = xmlTextWriterEndElement(writer);
	if (retval < 0) {
		fprintf(stderr, "xmlTextWriterEndElement failure: retval = %d\n", retval);
		goto cleanup;
	}

    return funcRetVal;
}

