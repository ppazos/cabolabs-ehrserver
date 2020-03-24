import com.cabolabs.ehrserver.versions.VersionFSRepoService
import com.cabolabs.ehrserver.versions.VersionS3RepoService
import com.cabolabs.ehrserver.openehr.OptFSService
import com.cabolabs.ehrserver.openehr.OptS3Service
import com.cabolabs.ehrserver.log.CommitLoggerFSService
import com.cabolabs.ehrserver.log.CommitLoggerS3Service

// Place your Spring DSL code here
beans = {
   // Configuration for using S3 file access
   versionRepoService(VersionS3RepoService)
   optService(OptS3Service)
   commitLoggerService(CommitLoggerS3Service)

   // Configuration for using File System file access
   // versionRepoService(VersionFSRepoService)
   // optService(OptFSService)
   // commitLoggerService(CommitLoggerFSService)
}
