// Mock of a broader subset of the GitHub REST API -- repos, issues, and
// pull requests -- used by the mcp_openapi "github" toolkit, described by
// ../etc/specs/github.openapi.yaml. Mutating endpoints require a Bearer
// credential; Zilla forwards the caller's own JWT via
// options.authorization.credentials.headers on the mcp_openapi binding,
// and this mock echoes the forwarded x-user-identity header back on
// pull request creation so the credential-forwarding path stays directly
// observable. Zilla's own OpenAPI-security enforcement already rejects an
// unscoped caller before a request reaches here, so the 401 checks below
// are defense in depth, not the primary gate.

import express from "express";

const PORT = Number(process.env.PORT ?? 4001);

const app = express();
app.use(express.json());

const requireBearer = (req, res) =>
{
    const authorization = req.headers["authorization"] ?? "";
    if (!authorization.startsWith("Bearer "))
    {
        res.status(401).json({ message: "Bad credentials" });
        return false;
    }
    return true;
};

const key = (owner, repo, id) => id === undefined ? `${owner}/${repo}` : `${owner}/${repo}/${id}`;

// --- repos -----------------------------------------------------------------

const repos = new Map();
repos.set(key("acme", "widget"), {
    name: "widget",
    full_name: "acme/widget",
    description: "Demo repository for the mcp.proxy example",
    homepage: null,
    private: false,
    default_branch: "main"
});

const branches = new Map();
branches.set(key("acme", "widget"), [
    { name: "main", protected: true },
    { name: "feature", protected: false }
]);

app.get("/repos/:owner/:repo", (req, res) =>
{
    const { owner, repo } = req.params;
    const found = repos.get(key(owner, repo));
    if (!found)
    {
        res.status(404).json({ message: "repository not found" });
        return;
    }
    res.json(found);
});

app.get("/repos/:owner/:repo/branches", (req, res) =>
{
    const { owner, repo } = req.params;
    res.json(branches.get(key(owner, repo)) ?? []);
});

// --- issues ------------------------------------------------------------------

let nextIssueNumber = 2;
const issues = new Map();
const issueComments = new Map();

issues.set(key("acme", "widget", 1), {
    number: 1,
    title: "Seed issue for the mcp.proxy example",
    state: "open",
    body: "Seeded so the issue_by_number resource has something to read."
});
issueComments.set(key("acme", "widget", 1), []);

app.get("/repos/:owner/:repo/issues", (req, res) =>
{
    const { owner, repo } = req.params;
    const { state } = req.query;
    const all = [...issues.entries()]
        .filter(([k]) => k.startsWith(`${owner}/${repo}/`))
        .map(([, v]) => v);
    res.json(state && state !== "all" ? all.filter((issue) => issue.state === state) : all);
});

app.post("/repos/:owner/:repo/issues", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo } = req.params;
    const { title, body } = req.body ?? {};
    const number = nextIssueNumber++;
    const issue = { number, title, state: "open", body: body ?? null };
    issues.set(key(owner, repo, number), issue);
    issueComments.set(key(owner, repo, number), []);
    res.status(201).json(issue);
});

app.get("/repos/:owner/:repo/issues/:number", (req, res) =>
{
    const { owner, repo, number } = req.params;
    const found = issues.get(key(owner, repo, number));
    if (!found)
    {
        res.status(404).json({ message: "issue not found" });
        return;
    }
    res.json(found);
});

app.patch("/repos/:owner/:repo/issues/:number", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo, number } = req.params;
    const found = issues.get(key(owner, repo, number));
    if (!found)
    {
        res.status(404).json({ message: "issue not found" });
        return;
    }

    const { title, body, state } = req.body ?? {};
    if (title !== undefined)
    {
        found.title = title;
    }
    if (body !== undefined)
    {
        found.body = body;
    }
    if (state !== undefined)
    {
        found.state = state;
    }
    res.json(found);
});

app.get("/repos/:owner/:repo/issues/:number/comments", (req, res) =>
{
    const { owner, repo, number } = req.params;
    res.json(issueComments.get(key(owner, repo, number)) ?? []);
});

let nextCommentId = 1;

app.post("/repos/:owner/:repo/issues/:number/comments", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo, number } = req.params;
    const comments = issueComments.get(key(owner, repo, number));
    if (!comments)
    {
        res.status(404).json({ message: "issue not found" });
        return;
    }

    const { body } = req.body ?? {};
    const comment = {
        id: nextCommentId++,
        body,
        user: req.headers["x-user-identity"] ?? "unknown"
    };
    comments.push(comment);
    res.status(201).json(comment);
});

// --- pulls ---------------------------------------------------------------

let nextPullNumber = 101;
const pulls = new Map();
const pullFiles = new Map();
const pullReviews = new Map();

// Seeded so pull_by_number, list_pull_files, and list_pull_reviews all have
// something to read without requiring a prior create_pr call.
pulls.set(key("acme", "widget", 42), {
    number: 42,
    html_url: "https://github.com/acme/widget/pull/42",
    state: "open",
    title: "Seed data for the pull_by_number resource demo"
});
pullFiles.set(key("acme", "widget", 42), [
    { filename: "src/index.js", additions: 10, deletions: 2, status: "modified" }
]);
pullReviews.set(key("acme", "widget", 42), []);

app.post("/repos/:owner/:repo/pulls", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo } = req.params;
    const { title, head, base } = req.body ?? {};
    const number = nextPullNumber++;
    const pull = {
        number,
        html_url: `https://github.com/${owner}/${repo}/pull/${number}`,
        state: "open",
        title,
        head,
        base,
        opened_by: req.headers["x-user-identity"] ?? "unknown"
    };

    pulls.set(key(owner, repo, number), pull);
    pullFiles.set(key(owner, repo, number), []);
    pullReviews.set(key(owner, repo, number), []);
    res.status(201).json(pull);
});

app.get("/repos/:owner/:repo/pulls", (req, res) =>
{
    const { owner, repo } = req.params;
    const { state } = req.query;
    const all = [...pulls.entries()]
        .filter(([k]) => k.startsWith(`${owner}/${repo}/`))
        .map(([, v]) => v);
    res.json(state && state !== "all" ? all.filter((pull) => pull.state === state) : all);
});

// Backs the mcp_openapi "pull_by_number" resource template
// (github+/repos/{owner}/{repo}/pulls/{number}) -- read-only, no credential
// check here, mirroring every other read operation in this mock.
app.get("/repos/:owner/:repo/pulls/:number", (req, res) =>
{
    const { owner, repo, number } = req.params;
    const pull = pulls.get(key(owner, repo, number));
    if (!pull)
    {
        res.status(404).json({ message: "pull request not found" });
        return;
    }

    res.json(pull);
});

app.patch("/repos/:owner/:repo/pulls/:number", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo, number } = req.params;
    const found = pulls.get(key(owner, repo, number));
    if (!found)
    {
        res.status(404).json({ message: "pull request not found" });
        return;
    }

    const { title, state } = req.body ?? {};
    if (title !== undefined)
    {
        found.title = title;
    }
    if (state !== undefined)
    {
        found.state = state;
    }
    res.json(found);
});

app.put("/repos/:owner/:repo/pulls/:number/merge", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo, number } = req.params;
    const found = pulls.get(key(owner, repo, number));
    if (!found)
    {
        res.status(404).json({ message: "pull request not found" });
        return;
    }

    found.state = "merged";
    res.json({
        merged: true,
        message: "Pull Request successfully merged",
        sha: `${owner}${repo}${number}`.padEnd(40, "0").slice(0, 40)
    });
});

app.get("/repos/:owner/:repo/pulls/:number/files", (req, res) =>
{
    const { owner, repo, number } = req.params;
    res.json(pullFiles.get(key(owner, repo, number)) ?? []);
});

app.get("/repos/:owner/:repo/pulls/:number/reviews", (req, res) =>
{
    const { owner, repo, number } = req.params;
    res.json(pullReviews.get(key(owner, repo, number)) ?? []);
});

let nextReviewId = 1;

app.post("/repos/:owner/:repo/pulls/:number/reviews", (req, res) =>
{
    if (!requireBearer(req, res))
    {
        return;
    }

    const { owner, repo, number } = req.params;
    const reviews = pullReviews.get(key(owner, repo, number));
    if (!reviews)
    {
        res.status(404).json({ message: "pull request not found" });
        return;
    }

    const { body, event } = req.body ?? {};
    const review = {
        id: nextReviewId++,
        body: body ?? null,
        event,
        user: req.headers["x-user-identity"] ?? "unknown"
    };
    reviews.push(review);
    res.status(201).json(review);
});

app.listen(PORT, () => console.log(`ghapi mock listening on ${PORT}`));
