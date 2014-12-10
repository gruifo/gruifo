package gruifo.mojo;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo( name = "generators", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class GruifoMojo extends AbstractMojo {

  /**
   *
   */
  @Parameter(required = true)
  private String[] includes;

  /**
   *
   */
  @Parameter
  private String typeMapperFile;


  /**
   * The Maven project instance for the executing project.
   */
  @Component
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    addGeneratedSourcesAsResource();
    addGeneratedSourcesToCompilePath();
  }

  private void addGeneratedSourcesAsResource() {
    final Resource resource = new Resource();
    resource.addInclude("");
    //    project.addResource(resource);
  }

  private void addGeneratedSourcesToCompilePath() {
    //    project.addCompileSourceRoot(path);
  }
}
