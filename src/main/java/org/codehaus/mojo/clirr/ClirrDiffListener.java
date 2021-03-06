package org.codehaus.mojo.clirr;

/*
 * Copyright 2006 The Codehaus
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.clirr.core.ApiDifference;
import net.sf.clirr.core.DiffListenerAdapter;
import net.sf.clirr.core.Severity;

/**
 * Listen to the Clirr events.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class ClirrDiffListener
    extends DiffListenerAdapter
{
    /**
     * The list of differences that occurred.
     */
    private List<ApiDifference> apiDifferences = new LinkedList<ApiDifference>();

    /**
     * The number messages for each severity.
     */
    private Map<Severity, Integer> counts = new HashMap<Severity, Integer>( 3 );

    private ApiDifferenceFilter[] filters;

    public ClirrDiffListener( ApiDifferenceFilter ... filters )
    {
        this.filters = filters;
    }

    @Override
    public void reportDiff( ApiDifference apiDifference )
    {
        if(shouldInclude(apiDifference))
        {
            incrementCount( apiDifference.getMaximumSeverity(), counts );
    
            apiDifferences.add( apiDifference );
        }
    }
    
    protected boolean shouldInclude( ApiDifference apiDifference )
    {
        for(ApiDifferenceFilter filter : filters)
        {
            if(!filter.shouldInclude( apiDifference ))
            {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void stop()
    {
        Collections.sort( apiDifferences, new Comparator<ApiDifference>()
        {
            public int compare( ApiDifference d1, ApiDifference d2 )
            {
                // compare maximum severities - order highest to lowest.
                return d2.getMaximumSeverity().compareTo( d1.getMaximumSeverity() );
            }
        } );
    }

    private void incrementCount( Severity sev, Map<Severity, Integer> counts )
    {
        if ( sev != null )
        {
            int count = getCount( counts, sev );
            counts.put( sev, new Integer( count + 1 ) );
        }
    }

    private int getCount( Map<Severity, Integer> counts, Severity sev )
    {
        Integer count = counts.get( sev );
        if ( count == null )
        {
            count = new Integer( 0 );
        }
        return count.intValue();
    }

    public List<ApiDifference> getApiDifferences()
    {
        return Collections.unmodifiableList( apiDifferences );
    }

    public int getSeverityCount( Severity severity )
    {
        return getCount( counts, severity );
    }

}
