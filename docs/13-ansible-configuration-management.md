# Task 13 — Ansible Configuration Management

## Target prerequisites
Use a disposable Ubuntu LTS VM/host with SSH and sudo/privilege escalation. Install Python on the managed node as required. Prefer SSH keys and known-host verification. Never run the classroom playbook against production.

Server specification:
- supported OS packages and runtime dependencies
- dedicated non-root service/deploy user
- application/release directories and explicit ownership/permissions
- Docker Engine if deploying containers
- Nginx if used as reverse proxy
- only required firewall ports
- service/container restart policy
- protected environment/config file
- logs and health check
- retained rollback artifact/image

## Suggested layout
~~~text
ansible/
  inventory/
    dev.ini
  group_vars/
    app.yml
  playbooks/
    configure.yml
  roles/
    common/
    docker/
    nginx/
    nutrisync/
~~~

A single playbook is acceptable for the first pass. Inventory should not contain passwords or private-key contents.

## Example inventory shape
~~~ini
[app]
nutrisync-dev ansible_host=192.0.2.10 ansible_user=deploy
~~~

192.0.2.10 is a documentation-only TEST-NET address, not a real target. Replace it locally with the disposable host. Commit a sanitized inventory example; keep sensitive details local.

## Playbook requirements
- Gather facts and fail on unsupported OS/version.
- Install packages with package modules.
- Create service group/user with least privilege.
- Create directories with explicit owner/mode.
- Configure Docker/Nginx and service/container declarations.
- Template non-secret settings.
- Use Ansible Vault or an approved secret manager.
- Start/enable services and validate health.
- Prefer native modules to shell/command.
- Use become only where required.

Never commit plaintext passwords in group_vars.

## First execution
~~~bash
ansible --version
ansible-inventory -i ansible/inventory/dev.ini --list
ansible -i ansible/inventory/dev.ini app -m ping
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/configure.yml --syntax-check
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/configure.yml --check --diff
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/configure.yml
~~~

Review check mode before applying; not every module fully supports it.

## Acceptance
- Inventory targets the intended disposable host.
- SSH and privilege escalation work.
- YAML syntax check passes.
- First execution completes or failure is diagnosed.
- Packages/users/folders/ports/services match specification.
- Secrets are absent from Git/logs.
- Sanitized inventory example, playbook and execution log captured.
