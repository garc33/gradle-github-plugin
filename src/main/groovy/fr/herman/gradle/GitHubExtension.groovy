package fr.herman.gradle


class GitHubExtension {
    String username
    String password
    String token
    String repo
    String branch = 'master'
    String message = 'upload with gradle-github-plugin'
}
