
/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
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

package com.cabolabs.util;

/**
 * Esta clase es util para medir la diferencia entre 2 fechas.
 * Se usa para saber la edad de una persona a partir de la fecha de nacimiento.
 */

import java.util.*;
import java.text.*;
 
public class DateDifference
{
    private static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
    private static final long MILLISECONDS_PER_YEAR = MILLISECONDS_PER_DAY * 365;
    
    private static final GregorianCalendar calendar = new GregorianCalendar();
    
    public static synchronized int numberOfMinutes(Date first, Date second)
    {
        return computeDifference( MILLISECONDS_PER_DAY, Calendar.MINUTE, first, second);
    }
    
    public static synchronized int numberOfDays(Date first, Date second)
    {
        return computeDifference( MILLISECONDS_PER_DAY, Calendar.DATE, first, second);
    }
    
    public static synchronized int numberOfYears(Date first, Date second)
    {
        return computeDifference( MILLISECONDS_PER_YEAR, Calendar.YEAR, first, second);
    }
    
    static private int computeDifference(long scale, int field, Date first, Date second)
    {
        long numberOfMilliseconds = second.getTime() - first.getTime();
        int value = (int)(numberOfMilliseconds / scale);
        
        calendar.setTime(first);
        calendar.add(field, value);
        
        while (calendar.getTime().before(second))
        {
            calendar.add(field, +1);
            value++;
        }
        while (calendar.getTime().after(second))
        {
            calendar.add(field, -1);
            value--;
        }
        return value;
    }
    
    static synchronized int daysSinceEpoch(Date date)
    {
        Date epoch = new Date(0);
        return numberOfDays(epoch, date);
    }
    
    public static void main(String[] args) throws Exception
    {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy MM dd");
        Date date = dateFormatter.parse("1000 1 1");
        Date now = new Date();
        System.out.println(date);
        {
            // Test days
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            for (int days = 0; calendar.getTime().before(now); days++)
            {
                int countOfDays = numberOfDays(date, calendar.getTime());
                if (countOfDays != days)
                    System.out.println(days + " " + countOfDays);
                
                countOfDays = -numberOfDays(calendar.getTime(), date);
                if (countOfDays != days)
                    System.out.println(days + " " + countOfDays);
                
                calendar.add(Calendar.DATE, +1);
            }
            System.out.println(numberOfDays(new Date(), new Date(System.currentTimeMillis() - 100)));
        }
        {
            // Test Years
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            for (int years = 0; calendar.getTime().before(now); years++)
            {
                int countOfYears = numberOfYears(date, calendar.getTime());
                if (countOfYears != years)
                    System.out.println(years + " " + countOfYears);
                
                countOfYears = -numberOfYears(calendar.getTime(), date);
                if (countOfYears != years)
                    System.out.println(years + " " + countOfYears);
                
                calendar.add(Calendar.YEAR, +1);
            }
            
            System.out.println(numberOfYears(dateFormatter.parse("1944 2 22"), now));
        }
    }
}

