DOCKER_IMAGE := artifactory.wikia-inc.com/services/vignette

# Injecting project version and build time
VERSION_GIT := $(shell sh -c 'git describe --always --tags')

CURRENT_DIR := $(shell pwd)

uberjar:
	lein uberjar

bumpversion:
	bumpversion patch

docker_build:
	docker build -f Dockerfile-k8s -t ${DOCKER_IMAGE}:$(VERSION_GIT) .

docker_show_image:
	@echo ${DOCKER_IMAGE}:${VERSION_GIT}

docker_upload:
	docker push ${DOCKER_IMAGE}:${VERSION_GIT}

.k8s:
	mkdir -p .k8s

docker_deploy: .k8s
	sed 's=$${IMAGE_PATH\}=${DOCKER_IMAGE}:${VERSION_GIT}=g' k8s/${K8S_DESCRIPTOR} > .k8s/${K8S_DESCRIPTOR}
	docker run -it --rm -v ${CURRENT_DIR}/.k8s/${K8S_DESCRIPTOR}:/${K8S_DESCRIPTOR} artifactory.wikia-inc.com/ops/k8s-deployer:0.0.15 kubectl apply -f /${K8S_DESCRIPTOR} -n $(NAMESPACE) --context=${K8S_CONTEXT}

docker_deploy_poz_dev:
	$(MAKE) docker_deploy K8S_DESCRIPTOR=k8s_descriptor-poz-dev.yaml K8S_CONTEXT=kube-poz-dev NAMESPACE=dev
