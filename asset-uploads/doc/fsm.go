package main

import (
	"flag"
	"fmt"
	"go/ast"
	"go/parser"
	"go/token"
	"io/ioutil"
	"strings"
)

type visitorFunc func(n ast.Node) ast.Visitor

func (f visitorFunc) Visit(n ast.Node) ast.Visitor {
	return f(n)
}

type visitor struct {
	state    string
	comments ast.CommentMap
}

func (s *visitor) Visit(n ast.Node) ast.Visitor {
	switch n := n.(type) {
	case (*ast.FuncDecl):
		// Only consider state* functions.
		if strings.HasPrefix(n.Name.Name, "state") {
			s.state = n.Name.Name
		} else {
			s.state = ""
		}
	case (*ast.ReturnStmt):
		// Not currently walking a relevant function, don't care about returns.
		if s.state == "" {
			return s
		}
		comments := s.comments.Filter(n).Comments()
		if len(n.Results) > 0 {
			if len(comments) > 0 {
				fmt.Printf("%s -> %s[label=\"%s\"]\n", s.state, n.Results[0], strings.TrimSpace(comments[0].Text()))
			} else {
				fmt.Printf("%s -> %s\n", s.state, n.Results[0])
			}
		}
	}

	return s
}

func main() {
	var (
		sourceFile = flag.String("source", "", "FSM .go source file")
	)
	flag.Parse()

	src, err := ioutil.ReadFile(*sourceFile)
	if err != nil {
		panic(err)
	}

	fset := token.NewFileSet()
	f, err := parser.ParseFile(fset, "src.go", src, 0|parser.ParseComments)
	if err != nil {
		panic(err)
	}

	cmap := ast.NewCommentMap(fset, f, f.Comments)

	fmt.Println(`// auto generated -- don't modify directly
digraph states {
node [shape=record colorscheme=set37 style="rounded,filled" fontname=sans fontsize=10];
edge [fontname="sans" fontsize=8];
rankdir=LR;
newrank=true;
fontname="sans";
color="grey";
labeljust="l";`)
	ast.Walk(&visitor{comments: cmap}, f)
	fmt.Println("}")
}
