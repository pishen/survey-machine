package pishen.test

import pishen.db.Record
import pishen.db.CitationMark

class TestRecord(val id: Int, rMap: Map[Int, Seq[Int]]) extends Record(null) {
  override def nodeId = id.toLong
  override def year = 2000
  override def citationType = CitationMark.Type.Number
  override def outgoingRecords = rMap(nodeId.toInt).map(l => new TestRecord(l, rMap))
  override def incomingRecords = 
    rMap.keys.filter(k => rMap(k).contains(nodeId)).map(l => new TestRecord(l, rMap)).toSeq
}