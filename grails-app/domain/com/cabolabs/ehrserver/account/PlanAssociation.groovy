package com.cabolabs.ehrserver.account

/**
 * Association of a plan for an organization. If "to" is not null, the plan is inactive.
 */
class PlanAssociation {

   String organizationUid
   Date from
   Date to
   Plan plan

   static constraints = {
      to ( nullable: true )
   }
   
   static mapping = {
     from column: "pa_from" // avoid using reserved word FROM
     to column: "pa_to" // avoid using reserved word TO in MySQL
   }
}
