package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import play.api.libs.json.{JsNumber, JsString, Json}

import scala.util.Random

class RulesFilterSpec extends UnitSpecification {

  trait Context extends Scope {
    val allowed = (1 to 2).map(id => new Urn(s"soundcloud:a:$id")).toSeq
    val allowedRules = allowed.map(new ContentAuthorization(_, ContentPolicy.from("allowed"), Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE))

    val snippet = (1 to 1).map(id => new Urn(s"soundcloud:s:$id")).toSeq
    val snippetRules = snippet.map(new ContentAuthorization(_, ContentPolicy.from("snippet"), Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE))

    val blocked = (1 to 2).map(id => new Urn(s"soundcloud:b:$id")).toSeq
    val blockedRules = blocked.map(new ContentAuthorization(_, ContentPolicy.from("blocked"), Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE))

    val monetize = (1 to 2).map(id => new Urn(s"soundcloud:m:$id")).toSeq
    val monetizeRules = monetize.map(new ContentAuthorization(_, ContentPolicy.from("monetize"), Reason.UNKNOWN, MonetizationModel.AD_SUPPORTED))

    def toJson(urns: Seq[Urn]) = urns.map {
      urn => Json.obj("urn" -> JsString(urn.toString))
    }

    val allJson = toJson(allowed ++ snippet ++ blocked ++ monetize).sortWith((_, _) => Random.nextBoolean())

    val allRules = allowedRules ++ snippetRules ++ blockedRules ++ monetizeRules
  }

  "json content without URNs" >> {
    "blocks all items" in {
      val jsonContent = (1 to 4).map {
        id => Json.obj("id" -> JsNumber(id))
      }.toSeq
      val rules = (1 to 4).map(i => new ContentAuthorization(new Urn(s"soundcloud:things:$i"), ContentPolicy.from("blocked"), Reason.NOT_SUPPORTED, MonetizationModel.NOT_APPLICABLE)).toSeq
      new RulesFilter(ContentPolicy.from("allowed")).filter(rules, jsonContent) must beEmpty
      new RulesFilter(ContentPolicy.from("allowed")).filterAndGetDetailedResult(rules, jsonContent) ==== RulesFilterResult(Seq.empty, Seq.empty, Seq.empty)
    }
  }

  "json content with URNs" >> {
    "returns only items matching whitelisted policies" in new Context {
      new RulesFilter(ContentPolicy.from("allowed"), ContentPolicy.from("monetize")).filter(allRules, allJson).map(_._1).sortBy(_.toString()) ==== toJson(allowed ++ monetize).sortBy(_.toString())

      val rulesFilterResult = new RulesFilter(ContentPolicy.from("allowed"), ContentPolicy.from("monetize")).filterAndGetDetailedResult(allRules, allJson)
      rulesFilterResult.allowedContent.map(_._1).sortBy(_.toString()) ==== toJson(allowed ++ monetize).sortBy(_.toString())
      rulesFilterResult.allowedUrns.sortBy(_.toString) ==== Seq(new Urn("soundcloud:a:1"), new Urn("soundcloud:a:2"), new Urn("soundcloud:m:1"), new Urn("soundcloud:m:2"))
      rulesFilterResult.filteredUrns.sortBy(_.toString) ==== Seq(new Urn("soundcloud:b:1"), new Urn("soundcloud:b:2"), new Urn("soundcloud:s:1"))
    }

    "returns empty if nothing matches policy" in new Context {
      new RulesFilter(ContentPolicy.from("allowed")).filter(allRules, toJson(monetize)).map(_._1).sortBy(_.toString()) must beEmpty

      val rulesFilterResult = new RulesFilter(ContentPolicy.from("allowed")).filterAndGetDetailedResult(allRules, toJson(monetize))
      rulesFilterResult.allowedContent must beEmpty
      rulesFilterResult.allowedUrns must beEmpty
      rulesFilterResult.filteredUrns.sortBy(_.toString) ==== Seq(new Urn("soundcloud:m:1"), new Urn("soundcloud:m:2"))

    }

    "returns items for which rules were not found" in new Context {
      new RulesFilter(ContentPolicy.from("allowed")).filter(Seq.empty, allJson).map(_._1).sortBy(_.toString()) ==== allJson.sortBy(_.toString())

      val rulesFilterResult = new RulesFilter(ContentPolicy.from("allowed")).filterAndGetDetailedResult(Seq.empty, allJson)
      rulesFilterResult.allowedContent.map(_._1).sortBy(_.toString()) ==== allJson.sortBy(_.toString())
      rulesFilterResult.allowedUrns.sortBy(_.toString) ==== Seq(
        new Urn("soundcloud:a:1"),
        new Urn("soundcloud:a:2"),
        new Urn("soundcloud:b:1"),
        new Urn("soundcloud:b:2"),
        new Urn("soundcloud:m:1"),
        new Urn("soundcloud:m:2"),
        new Urn("soundcloud:s:1")
      )
      rulesFilterResult.filteredUrns must beEmpty
    }
  }
}
