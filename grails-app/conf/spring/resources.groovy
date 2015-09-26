// Place your Spring DSL code here
import com.cabolabs.security.AuthFilter
import com.cabolabs.security.AuthProvider
beans = {

   authProvider(AuthProvider) {
   }

   authFilter(AuthFilter) {
      // properties
      // http://www.oodlestechnologies.com/blogs/Adding-Custom-Spring-Security-Authentication
      authenticationManager = ref("authenticationManager")
      authProvider = ref("authProvider")
   }
   
   
}
