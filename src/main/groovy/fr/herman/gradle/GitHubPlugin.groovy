package fr.herman.gradle

import static java.lang.Integer.MAX_VALUE
import static org.eclipse.egit.github.core.Blob.ENCODING_BASE64
import static org.eclipse.egit.github.core.TreeEntry.MODE_BLOB
import static org.eclipse.egit.github.core.TreeEntry.TYPE_BLOB
import static org.eclipse.egit.github.core.TypedResource.TYPE_COMMIT
import org.eclipse.egit.github.core.Blob
import org.eclipse.egit.github.core.Commit
import org.eclipse.egit.github.core.CommitUser
import org.eclipse.egit.github.core.Reference
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.TreeEntry
import org.eclipse.egit.github.core.TypedResource
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.UserService
import org.eclipse.egit.github.core.util.EncodingUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.mvn3.org.codehaus.plexus.util.DirectoryScanner

class GitHubPlugin implements Plugin<Project> {

    void apply(Project project) {

        project.extensions.create('github', GitHubExtension)

        project.apply plugin: 'maven'

        project.uploadArchives {
            repositories.mavenDeployer { repository(url: "file:///${project.buildDir}/github") }
        }

        project.task('uploadGithub', dependsOn: 'uploadArchives') <<{
            RepositoryId repository = RepositoryId.create(project.github.username,project.github.repo)

            def client = new GitHubClient()
            if(project.github.token)
                client.setOAuth2Token(project.github.token)
            else
                client.setCredentials(project.github.username, project.github.password)
            def service = new DataService(client);
            def entries = []
            def directory = new File(project.buildDir.path + '/github')
            def paths = getMatchingPaths(directory.path)
            def prefix = 'maven/' + (project.version.toString().toUpperCase().contains('SNAPSHOT')?'snapshots':'releases')
            for(String path : paths){
                def file = new File(directory,path)
                project.logger.info("Upload file: ${file}")
                def blob = service.createBlob(repository, new Blob().setEncoding(ENCODING_BASE64).setContent(EncodingUtils.toBase64(file.readBytes())))
                entries << new TreeEntry().setPath(prefix + '/' + path.replace('\\', '/')).setType(TYPE_BLOB).setMode(MODE_BLOB).setSha(blob)
            }
            def ref = service.getReference(repository, 'refs/heads/'+project.github.branch)
            def tree = service.createTree(repository, entries,ref?service.getCommit(repository,ref.getObject().getSha()).getTree()?.getSha():null)

            Commit commit = new Commit()
            commit.setMessage(project.github.message)
            commit.setTree(tree);
            UserService userService = new UserService(service.getClient());
            User user = userService.getUser();
            CommitUser author = new CommitUser().setName(user.getName()).setEmail(user.getEmail()).setDate(new GregorianCalendar().getTime())
            commit.setAuthor(author);
            commit.setCommitter(author);
            if (ref)
                commit.setParents([new Commit().setSha(ref.getObject().getSha())]);
            def created = service.createCommit(repository, commit);

            TypedResource object = new TypedResource().setType(TYPE_COMMIT).setSha(created.getSha())
            if (ref) {
                ref.setObject(object);
                service.editReference(repository, ref);
            } else {
                service.createReference(repository, new Reference().setObject(object).setRef('refs/heads/'+project.github.branch));
            }
        }
    }

    def String[] getMatchingPaths(String baseDir) {
        DirectoryScanner scanner = new DirectoryScanner()
        scanner.setBasedir(baseDir);
        scanner.scan();
        return scanner.getIncludedFiles();
    }
}
