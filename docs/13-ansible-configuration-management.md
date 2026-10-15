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
    site.yml
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

## Optional initial staff account
The application exposes an opt-in bootstrap for the first nutritionist account. The Ansible playbook keeps it disabled by default. When required for a fresh non-demo deployment, provide `NUTRISYNC_BOOTSTRAP_STAFF_ENABLED=true`, `NUTRISYNC_BOOTSTRAP_STAFF_NAME`, `NUTRISYNC_BOOTSTRAP_STAFF_EMAIL` and a unique `NUTRISYNC_BOOTSTRAP_STAFF_PASSWORD` of 20–72 characters via the control node's protected environment/secret manager. The playbook passes these values to the app container under `no_log`; do not place them in a committed inventory. Bootstrap creates an account only if absent and does not reset an existing password. Disable/remove the temporary secret values after provisioning.

## First execution
~~~bash
ansible --version
ansible-inventory -i ansible/inventory/dev.ini --list
ansible -i ansible/inventory/dev.ini app -m ping
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --syntax-check
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --check --diff
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
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
