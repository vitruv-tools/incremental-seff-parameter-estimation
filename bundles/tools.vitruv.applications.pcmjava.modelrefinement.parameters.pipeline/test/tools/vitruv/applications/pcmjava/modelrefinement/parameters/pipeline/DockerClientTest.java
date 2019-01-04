package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

public class DockerClientTest {

	public static void main(String[] args) throws DockerException, InterruptedException {
		// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
		final DockerClient docker = DefaultDockerClient.builder().uri("unix:///var/run/docker.sock").build();

		docker.listContainers().forEach(container -> {
			System.out.println(container.image());
		});

		docker.close();
	}

}
